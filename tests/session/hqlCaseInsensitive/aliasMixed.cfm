<cfscript>
// Alias chain `c.UserName` (entity case-correct, alias case preserved)
// This must work in BOTH modes — exact-case HQL is always valid.
try {
	result = ormExecuteQuery( "from Customer c where c.UserName = :n", { n: "BobBrown" } );
	if ( !isArray( result ) || arrayLen( result ) != 1 )
		throw( message="expected 1 result, got #arrayLen( result )#" );
	if ( result[ 1 ].getUserName() != "BobBrown" )
		throw( message="expected BobBrown, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
