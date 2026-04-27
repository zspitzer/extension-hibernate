<cfscript>
// String literal 'JohnSmith' must NOT be lowercased by the HQL pre-processor —
// only identifier tokens are normalised. Property name uses lowercase to also
// exercise property-case bridging; literal stays case-exact.
try {
	result = ormExecuteQuery( "from Customer where username = 'JohnSmith'" );
	if ( !isArray( result ) || arrayLen( result ) != 1 )
		throw( message="expected 1 result, got #arrayLen( result )#" );
	if ( result[ 1 ].getUserName() != "JohnSmith" )
		throw( message="expected JohnSmith, got #result[ 1 ].getUserName()#" );
	echo( "ok" );
} catch( any e ) {
	echo( e.message );
}
</cfscript>
