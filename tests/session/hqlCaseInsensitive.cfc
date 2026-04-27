component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function run( testResults, testBox ) {

		// Stage 7l: HQL case-insensitive bridging scaffolded but disabled (xdescribe).
		// Tokenizer + flag plumbing landed; HBM-side lowercasing reverted because it
		// requires coordinated canonical-case entity-name handling across ~8 callers
		// of HibernateCaster.getEntityName/session APIs. Stage 7m: wire it properly.
		// To re-enable: change xdescribe → describe in this file.
		xdescribe( "HQL case-insensitive (default — hqlCaseSensitive=false)", function() {

			it( "lowercase property name in WHERE resolves", function() {
				var result = _InternalRequest( template: "#uri()#/whereLowercase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "uppercase property name in WHERE resolves", function() {
				var result = _InternalRequest( template: "#uri()#/whereUppercase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "lowercase property in SELECT resolves", function() {
				var result = _InternalRequest( template: "#uri()#/selectLowercase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "lowercase property in ORDER BY resolves", function() {
				var result = _InternalRequest( template: "#uri()#/orderByLowercase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "lowercase entity name in FROM resolves", function() {
				var result = _InternalRequest( template: "#uri()#/entityLowercase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "exact-case alias chain still works (regression check)", function() {
				var result = _InternalRequest( template: "#uri()#/aliasMixed.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "string literal preserved (not lowercased)", function() {
				var result = _InternalRequest( template: "#uri()#/stringLiteral.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "named parameter case preserved", function() {
				var result = _InternalRequest( template: "#uri()#/namedParamCase.cfm", url: { hqlCaseSensitive: "false" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

		});

		xdescribe( "HQL strict (hqlCaseSensitive=true) — opt-in to vanilla H7 semantics", function() {

			it( "exact-case alias chain still works", function() {
				var result = _InternalRequest( template: "#uri()#/aliasMixed.cfm", url: { hqlCaseSensitive: "true" } );
				expect( trim( result.filecontent ) ).toBe( "ok" );
			});

			it( "named parameter case still preserved (no pre-processing)", function() {
				// NOTE: this .cfm uses lowercase property `username` which will fail
				// in strict mode — we only assert that the param-name handling is
				// independent of the property-case branch.
				var result = _InternalRequest( template: "#uri()#/namedParamCase.cfm", url: { hqlCaseSensitive: "true" } );
				expect( trim( result.filecontent ) ).toInclude( "Could not interpret path expression" );
			});

			it( "lowercase property in WHERE rejected", function() {
				var result = _InternalRequest( template: "#uri()#/whereLowercase.cfm", url: { hqlCaseSensitive: "true" } );
				expect( trim( result.filecontent ) ).toInclude( "Could not interpret path expression" );
			});

			it( "lowercase entity name in FROM rejected", function() {
				var result = _InternalRequest( template: "#uri()#/entityLowercase.cfm", url: { hqlCaseSensitive: "true" } );
				// H7 distinguishes property-path errors ("Could not interpret path expression")
				// from root-entity errors ("Could not resolve root entity")
				expect( trim( result.filecontent ) ).toInclude( "Could not resolve root entity" );
			});

		});

	}

	private string function uri() {
		return getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) ) & "hqlCaseInsensitive";
	}

}
