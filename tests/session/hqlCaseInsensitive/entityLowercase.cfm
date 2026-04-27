<cfscript>
// Entity name `customer` should resolve to entity `Customer`
try {
	result = ormExecuteQuery( "from customer" );
	if ( !isArray( result ) || arrayLen( result ) != 3 )
		throw( message="expected 3 results, got #arrayLen( result )#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
