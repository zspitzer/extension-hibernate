<cfscript>
// HQL property `USERNAME` should resolve to entity property `UserName`
try {
	result = ormExecuteQuery( "from Customer where USERNAME = :n", { n: "JaneDoe" } );
	if ( !isArray( result ) || arrayLen( result ) != 1 )
		throw( message="expected 1 result, got #arrayLen( result )#" );
	if ( result[ 1 ].getUserName() != "JaneDoe" )
		throw( message="expected JaneDoe, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
