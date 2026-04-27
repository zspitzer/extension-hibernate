<cfscript>
// SELECT clause property case mismatch
try {
	result = ormExecuteQuery( "select c.username from Customer c order by c.id" );
	if ( !isArray( result ) || arrayLen( result ) != 3 )
		throw( message="expected 3 results, got #arrayLen( result )#" );
	if ( result[ 1 ] != "JohnSmith" )
		throw( message="expected JohnSmith, got #result[ 1 ]#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
