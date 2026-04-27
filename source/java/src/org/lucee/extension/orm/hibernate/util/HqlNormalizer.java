package org.lucee.extension.orm.hibernate.util;

/**
 * Lowercases identifier tokens in HQL strings while preserving string literals,
 * named/positional parameters, and numeric literals. Bridges CFML's case-insensitive
 * semantics to Hibernate 7's strict HQL parser.
 *
 * Paired with HBMCreator emitting lowercased property/entity names — both sides flip
 * together via the {@code hqlCaseSensitive} ORM setting.
 */
public final class HqlNormalizer {

	private HqlNormalizer() {}

	/**
	 * Returns {@code hql} with all identifier tokens lowercased. String literals,
	 * named parameters ({@code :name}), and numeric literals are preserved verbatim.
	 *
	 * Idempotent: {@code normalize(normalize(x)).equals(normalize(x))}.
	 *
	 * @param hql the HQL/JPQL string to normalise; null/empty returned as-is
	 * @return normalised HQL
	 */
	public static String normalize(String hql) {
		if (hql == null || hql.isEmpty()) return hql;
		final int len = hql.length();
		final StringBuilder out = new StringBuilder(len);
		int i = 0;
		while (i < len) {
			char c = hql.charAt(i);

			// single-quoted string literal — preserve verbatim, honouring '' as escape
			if (c == '\'') {
				int start = i++;
				while (i < len) {
					char d = hql.charAt(i);
					if (d == '\'') {
						if (i + 1 < len && hql.charAt(i + 1) == '\'') {
							i += 2;
							continue;
						}
						i++;
						break;
					}
					i++;
				}
				out.append(hql, start, i);
				continue;
			}

			// named parameter (:name) — preserve case (Hibernate is case-sensitive on param names)
			if (c == ':') {
				out.append(c);
				i++;
				while (i < len) {
					char d = hql.charAt(i);
					if (Character.isLetterOrDigit(d) || d == '_') {
						out.append(d);
						i++;
					} else break;
				}
				continue;
			}

			// identifier token — lowercase. Java identifier rules cover ASCII letters, digits, $ and _
			if (Character.isJavaIdentifierStart(c)) {
				while (i < len && Character.isJavaIdentifierPart(hql.charAt(i))) {
					out.append(Character.toLowerCase(hql.charAt(i)));
					i++;
				}
				continue;
			}

			// numeric literal — preserve verbatim (digits, decimal point, scientific notation)
			if (Character.isDigit(c)) {
				while (i < len) {
					char d = hql.charAt(i);
					if (Character.isLetterOrDigit(d) || d == '.') {
						out.append(d);
						i++;
					} else break;
				}
				continue;
			}

			// whitespace, operators, punctuation — pass through
			out.append(c);
			i++;
		}
		return out.toString();
	}
}
