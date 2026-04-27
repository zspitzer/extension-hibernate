<cfscript>
// Named parameter `:UserName` must NOT be touched by the pre-processor — Hibernate
// is case-sensitive on parameter names. The CFML struct key matches by case-insensitive
// lookup so `params.UserName` and `:UserName` line up regardless.
try {
	result = ormExecuteQuery( "from Customer where username = :UserName", { UserName: "JaneDoe" } );
	if ( !isArray( result ) || arrayLen( result ) != 1 )
		throw( message="expected 1 result, got #arrayLen( result )#" );
	if ( result[ 1 ].getUserName() != "JaneDoe" )
		throw( message="expected JaneDoe, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
