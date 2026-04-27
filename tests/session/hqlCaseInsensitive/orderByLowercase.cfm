<cfscript>
// ORDER BY clause property case mismatch
try {
	result = ormExecuteQuery( "from Customer c order by c.username" );
	if ( !isArray( result ) || arrayLen( result ) != 3 )
		throw( message="expected 3 results, got #arrayLen( result )#" );
	// alphabetical by UserName: BobBrown, JaneDoe, JohnSmith
	if ( result[ 1 ].getUserName() != "BobBrown" )
		throw( message="expected first BobBrown, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
