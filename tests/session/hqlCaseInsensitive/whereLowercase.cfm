<cfscript>
// HQL property `username` should resolve to entity property `UserName`
try {
	result = ormExecuteQuery( "from Customer where username = :n", { n: "JohnSmith" } );
	if ( !isArray( result ) || arrayLen( result ) != 1 )
		throw( message="expected 1 result, got #arrayLen( result )#" );
	if ( result[ 1 ].getUserName() != "JohnSmith" )
		throw( message="expected JohnSmith, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
