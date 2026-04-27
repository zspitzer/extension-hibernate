<cfscript>
// Verify ORMGetSession() returns native org.hibernate.Session (not a wrapper like ACF)
ormSess = ORMGetSession();
className = ormSess.getClass().getName();
if ( className does not contain "hibernate" )
	throw( message="expected hibernate ormSess class, got #className#" );

// Verify ormSess.isOpen()
if ( !ormSess.isOpen() )
	throw( message="ormSess should be open" );

// Verify ORMGetSessionFactory returns native factory
factory = ORMGetSessionFactory();
factoryClass = factory.getClass().getName();
if ( factoryClass does not contain "hibernate" )
	throw( message="expected hibernate factory class, got #factoryClass#" );

// Hibernate 7 removed SessionFactory.getClassMetadata(name); replacement is the
// MappingMetamodel via the SessionFactoryImplementor cast.
descriptor = factory.getMappingMetamodel().findEntityDescriptor( "SmokeEntity" );
if ( isNull( descriptor ) )
	throw( message="findEntityDescriptor should return metadata for SmokeEntity" );

// Verify we can get property names from the entity descriptor
propNames = descriptor.getPropertyNames();
if ( !isArray( propNames ) || arrayLen( propNames ) == 0 )
	throw( message="expected property names array" );

echo( "ok" );
</cfscript>
