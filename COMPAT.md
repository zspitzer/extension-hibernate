# Compatibility Layer

The H5.6 → H7.3 jump removed a substantial slice of the Hibernate API that real-world CFML code depends on (cborm, ColdBox, Slatwall, basecfc, ModelGlue, and all their derivatives). This extension wraps the Hibernate objects it returns to CFML so the legacy API surface keeps resolving where it can.

This document describes the compat layer: what's currently shimmed, why, how to extend it, and when NOT to.

## Philosophy

**API-shape preservation, not behaviour preservation.** When Hibernate removes a method, we re-expose its name and route to the closest H7 equivalent. CFML callers see "the method exists and returns sensible data." Where the H5 contract differs from the H7 replacement (e.g. `merge` returns a managed copy that may not be `==` the input where `update` returned void), the difference is documented in [BREAKING-CHANGES.md](BREAKING-CHANGES.md). Shim shape, document semantics.

**Empirical, not aspirational.** Every shimmed method is justified by either a reflection-derived diff of `ormGetSession()` / `ormGetSessionFactory()` between Lucee+5.6 and Lucee+7.3, or a `gh search code` survey of real-world CFML callers. We don't pre-emptively shim every removed method — that grows the wrapper into a museum of dead APIs we'd own forever.

**The pattern is finite, not a treadmill.** Each Hibernate major-version bump deletes a discrete set of methods. We address each round with a wrapper sized to the actual surface, not a framework that anticipates every future deletion. Two wrappers (SessionFactory shipped, Session spec'd) cover the H7 round. H8's deletions get measured the same way.

## Currently shipped: `CompatSessionFactoryWrapper`

Wraps the `SessionFactory` returned by `ormGetSessionFactory()`. Implemented as a `java.lang.reflect.Proxy` declaring both the synthetic `CompatSessionFactory` interface (legacy method names) and `SessionFactoryImplementor` (modern SPI). CFML reflection sees both surfaces.

Source: [`source/java/src/org/lucee/extension/orm/hibernate/compat/`](source/java/src/org/lucee/extension/orm/hibernate/compat/)

| Method | H7 routing | Notes |
| ------ | ---------- | ----- |
| `getDialect()` | `SFI.getJdbcServices().getDialect()` | Pass-through |
| `getClassMetadata(String)` | `SFI.getMappingMetamodel().findEntityDescriptor(name)` | Returns `EntityPersister` (replaces deleted `ClassMetadata`); H5 returned null on unknown name, H7 also returns null — no translation needed |
| `getCollectionMetadata(String)` | `SFI.getMappingMetamodel().findCollectionDescriptor(role)` | Returns `CollectionPersister`; H5 throws `MappingException` on unknown, H7 returns null — shim translates null → `MappingException` |
| `getEntityPersister(String)` | Same as `getClassMetadata` — `findEntityDescriptor(name)` | H5 alias used by cborm `SQLHelper`/`ORMUtilSupport` and ColdBox legacy ORM helpers; H5 throws on unknown — shim translates null → `MappingException` |

### Deliberately NOT shimmed

- `getAllClassMetadata()` — already throws `UnsupportedOperationException` from H5.6.15 itself. No behaviour to preserve.
- `getAllCollectionMetadata()` — same situation as above.
- `getTypeHelper()` — zero observed callers (per `gh search code` survey). Anyone who needs it can use `sf.getTypeConfiguration().getBasicTypeRegistry()` directly.
- ~30+ other internal SPI removals (`configuredInterceptor`, `getFastSessionServices`, `getNamedQueryRepository`, `registerNamedQueryDefinition`, etc.) — Hibernate-internal SPI that CFML applications don't reach for.

### Real-world callers (verified via `gh search code`)

Surveyed 2026-04-29 against public GitHub. The shimmed methods cover known callers in:

- **cborm** — `models/util/support/ORMUtilSupport.cfc`, `models/criterion/BaseBuilder.cfc`, `models/sql/SQLHelper.cfc`
- **Slatwall** — `org/Hibachi/HibachiService.cfc`
- **ColdBox legacy** — `system/orm/hibernate/BaseORMService.cfc`, `system/orm/hibernate/sql/SQLHelper.cfc`, `system/orm/hibernate/DetachedCriteriaBuilder.cfc`
- **basecfc** — `base.cfc`
- **ColdMVC** — `model/ModelManager.cfc`
- **databoss** — `models/MetadataService.cfc`
- **mementifier** — `interceptors/Mementifier.cfc`

Plus the corresponding test-harness apps under `coldbox-samples`, `coldbox-modules`, and many derivative apps that vendor copies of cborm or ColdBox ORM.

## Planned: `CompatSessionWrapper`

Spec'd but not yet implemented. Wraps the `Session` returned by `ormGetSession()`. Hybrid design:

- **Hard-coded mechanical translations** — for families with deterministic JPA mappings (`save`/`update`/`saveOrUpdate`/`delete`/`load`/`refresh`/`createSQLQuery`/`getNamedSQLQuery`). Routes directly in Java, no consumer config needed.
- **Plug-in CFC hook** — for `createCriteria` and similar opinionated surfaces where the H7 replacement is a wrapper API consumers want to control. Set `ormSettings.sessionShim` to a CFC path; the wrapper dispatches matching method names to that CFC.

Empirical surface: **73 methods removed** from `Session` in H5.6 → H7.3, of which **32 across 9 families** are CFML-relevant. Spec lives in your tracker (`h73-session-shim-spec.md`).

## Methodology: the reflection dump

The empirical surface for both wrappers came from running [`tests/compat/sessionApiSurface/`](tests/compat/sessionApiSurface/) twice — once with the released H5.6 extension installed, once with the local H7.3 build — and diffing the output of `getClass().getMethods()` on `ormGetSession()` (and a sibling script for `ormGetSessionFactory()`).

```bash
# H7.3 (current build)
ant -buildfile "d:\work\script-runner\build.xml" \
    -DluceeVersion="7.0/snapshot/light" \
    -Dwebroot="tests/compat/sessionApiSurface" \
    -Dexecute="index.cfm" \
    -DextensionDir="target" \
    -DuniqueWorkingDir="true" > test-output/session-api-h73.txt 2>&1

# H5.6 (released extension by GUID)
ant -buildfile "d:\work\script-runner\build.xml" \
    -DluceeVersion="7.0/snapshot/light" \
    -Dwebroot="tests/compat/sessionApiSurface" \
    -Dexecute="index.cfm" \
    -Dextensions="FAD1E8CB-4F45-4184-86359145767C29DE" \
    -DuniqueWorkingDir="true" > test-output/session-api-h56.txt 2>&1

# Extract sorted method lists, diff
sed -n '/=== Methods/,$p' test-output/session-api-h56.txt \
    | sed -E 's/^.*\[script\] //' \
    | grep -E '^[a-zA-Z_][a-zA-Z0-9_]*\(' | sort -u > h56.methods
# (same for h73)
comm -23 h56.methods h73.methods   # methods removed in H7.3
```

Run `sessionFactory.cfm` instead of `index.cfm` to dump the SessionFactory surface.

The methodology is reusable. When H8 lands, run it again — H7.3 baseline vs H8 build — and the new removal list falls out automatically. Same applies to `Query`, `Transaction`, or any other Hibernate type CFML reflects on.

## Adding a new shim method (TDD pattern)

The existing wrappers grew by adding one method at a time, locked down by a probe test before any Java change. The flow:

1. **Identify the gap.** Reflection diff or real-world report says "method X disappeared and consumer Y is calling it."
2. **GH-search the candidate.** Confirm there are real callers (`gh search code --extension cfc 'methodName'`). If there are none, leave it broken — premature shimming is how dead-API debt accumulates.
3. **Probe test.** Add an assertion to the relevant test (`tests/session/apiSmoke/legacyShim.cfm` for SessionFactory shim methods; new test files for new families). The test should call the legacy method and assert reasonable behaviour. Run on the 5.6 branch first to lock down the H5 contract; cherry-pick to 7.0 to confirm TDD red.
4. **Implement.** Add the method signature to `CompatSessionFactory`/`CompatSession`, add a routing branch in the corresponding wrapper, document any null/exception translation needed to match H5 semantics.
5. **Confirm green.** Re-run; the probe should pass.
6. **Document semantics.** If the H7 routing changes any behaviour vs H5, add a note to [BREAKING-CHANGES.md](BREAKING-CHANGES.md). The shim preserves shape, not always semantics.

## When NOT to shim

- **The method is already broken on H5.** `getAllClassMetadata()` threw `UnsupportedOperationException` from H5.6.15. Anyone calling it has been broken upstream for years; there's nothing to preserve.
- **Zero real-world callers.** GH search returns zero CFML hits. Re-implementing a deleted API for hypothetical users grows maintenance debt.
- **The method is internal SPI.** Methods on `SessionFactoryImplementor` / `SessionImplementor` that aren't on the public `SessionFactory` / `Session` interfaces are extension points for ORM internals, not user code. CFML doesn't reach them.
- **The replacement requires opinionated wrapper semantics.** `Session.createCriteria(name)` returned a Hibernate-native `Criteria` object whose closest replacement is "build a JPA `CriteriaQuery` and wrap it." That wrapping is consumer-opinionated (cborm wants `cborm.models.criterion.jpa.CriteriaBuilder`; ColdBox might want something else). Hard-coding our pick would impose policy on every consumer. This is what the planned plug-in CFC hook is for.
- **The behaviour change isn't an API-shape change.** H6's distinct-collection-fetch behaviour change, H7.3's strict-readonly-collections enforcement, etc. — these flow through to user code regardless of shimming. Document in [BREAKING-CHANGES.md](BREAKING-CHANGES.md), don't try to shim them.

## Forward-looking

The compat layer's job is finite. Each Hibernate major version bump produces a discrete removal list; we measure it, shim what's CFML-relevant, document what isn't shimmable. Two wrappers cover the H7 round. H8 is unscheduled; when it lands, we'll run the dump twice and the next list of additions will fall out.

What this layer **does not** promise:

- That every CFML codebase will run unchanged across Hibernate major versions. Behaviour changes (default fetch joins, cascade semantics, error rounding) are out of scope.
- That every removed Hibernate method will eventually be shimmed. We shim what consumers call.
- Forever maintenance of any specific API surface. If a shimmed method's H7 routing breaks in H8 (Hibernate keeps removing things), we re-evaluate at that point.

What it **does** promise:

- Common ColdBox/cborm/Slatwall metadata-introspection patterns keep working transparently across the H5 → H7 jump.
- The pattern is documented and extensible — adding the next shim method is a one-day TDD job, not a research project.
- Lucee can credibly tell its ORM users: "We absorb most Hibernate API breakage. Here's the layer that does it. Here's how to find new gaps."
