package org.lucee.extension.orm.hibernate;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.lucee.extension.orm.hibernate.util.CommonUtil;
import org.osgi.framework.Bundle;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.db.DataSource;
import lucee.runtime.type.Struct;
import lucee.runtime.util.ListUtil;

/**
 * Hibernate Dialect manager
 */
public class Dialect {

	private Dialect() {}

	private static Struct dialects = CommonUtil.createStruct();

	static {

		try {
			BundleClassLoader bcl = (BundleClassLoader) org.hibernate.dialect.SybaseDialect.class.getClassLoader();
			Bundle b = bcl.getBundle();

			// List all XML files in the OSGI-INF directory and below
			ListUtil util = CFMLEngineFactory.getInstance().getListUtil();
			Enumeration<URL> e = b.findEntries("org/hibernate/dialect", "*.class", true);
			String path;
			while (e.hasMoreElements()) {
				try {
					path = e.nextElement().getPath();
					if (path.startsWith("/")) path = path.substring(1);
					else if (path.startsWith("\\")) path = path.substring(1);
					if (path.endsWith(".class")) path = path.substring(0, path.length() - 6);
					path = path.replace('/', '.');
					path = path.replace('\\', '.');
					String name;
					Class<?> clazz = bcl.loadClass(path);
					if (org.hibernate.dialect.Dialect.class.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
						dialects.setEL(CommonUtil.createKey(path), path);
						dialects.setEL(CommonUtil.createKey(CommonUtil.last(path, ".")), path);
						name = CommonUtil.last(path, ".");
						dialects.setEL(CommonUtil.createKey(name), path);
						if (name.endsWith("Dialect")) {
							name = name.substring(0, name.length() - 7);
							dialects.setEL(CommonUtil.createKey(name), path);
						}

						// print.e("dialects.setEL(\"" + name + "\",\"" + path + "\");");
					}
				}
				catch (Exception exx) {
					exx.printStackTrace();
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		// Hibernate 7.3 deleted all version-specific dialect classes; the surviving
		// per-vendor dialect detects the server version via JDBC metadata at SF build.
		// The OSGi scan above auto-registers what's actually in the core jar (FQN +
		// simple name + short name). Below: legacy aliases only — historical names
		// that no longer have a matching class, mapped to their surviving family.
		// Names from `hibernate-community-dialects` (Derby, Firebird, Informix, Ingres,
		// Teradata, TimesTen, CUBRID, Cache71, Mckoi, etc.) are intentionally absent —
		// we do not ship that artifact, so resolution returns null with a clear error.

		// Oracle — Oracle8i/9/9i/10g/12c all gone, only OracleDialect survives
		String oracle = "org.hibernate.dialect.OracleDialect";
		dialects.setEL(CommonUtil.createKey("Oracle8i"), oracle);
		dialects.setEL(CommonUtil.createKey("Oracle9"), oracle);
		dialects.setEL(CommonUtil.createKey("Oracle9i"), oracle);
		dialects.setEL(CommonUtil.createKey("Oracle10g"), oracle);
		dialects.setEL(CommonUtil.createKey("Oracle12c"), oracle);
		dialects.setEL(CommonUtil.createKey("DataDirectOracle9"), oracle);

		// SQLServer — 2005/2008/2012 all gone, only SQLServerDialect survives
		String sqlServer = "org.hibernate.dialect.SQLServerDialect";
		dialects.setEL(CommonUtil.createKey("SQLServer2005"), sqlServer);
		dialects.setEL(CommonUtil.createKey("SQLServer2008"), sqlServer);
		dialects.setEL(CommonUtil.createKey("SQLServer2012"), sqlServer);

		// PostgreSQL — 81/82/9/91/92/93/94/95/10 all gone, only PostgreSQLDialect survives
		String postgres = "org.hibernate.dialect.PostgreSQLDialect";
		dialects.setEL(CommonUtil.createKey("PostgreSQL81"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL82"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL9"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL91"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL92"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL93"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL94"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL95"), postgres);
		dialects.setEL(CommonUtil.createKey("PostgreSQL10"), postgres);

		// MySQL — every version-specific dialect gone, only MySQLDialect survives (Stage 7h)
		String mysql = "org.hibernate.dialect.MySQLDialect";
		dialects.setEL(CommonUtil.createKey("MySQL5"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQL55"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQL57"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQL5InnoDB"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQL57InnoDB"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQL8"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQLInnoDB"), mysql);
		dialects.setEL(CommonUtil.createKey("MySQLMyISAM"), mysql);

		// MariaDB — 10/53/102/103 all gone, only MariaDBDialect survives
		String mariaDb = "org.hibernate.dialect.MariaDBDialect";
		dialects.setEL(CommonUtil.createKey("MariaDB10"), mariaDb);
		dialects.setEL(CommonUtil.createKey("MariaDB53"), mariaDb);
		dialects.setEL(CommonUtil.createKey("MariaDB102"), mariaDb);
		dialects.setEL(CommonUtil.createKey("MariaDB103"), mariaDb);

		// Sybase — Sybase11 / SybaseAnywhere gone; SybaseASE15/157 collapse to SybaseASEDialect
		String sybaseAse = "org.hibernate.dialect.SybaseASEDialect";
		dialects.setEL(CommonUtil.createKey("SybaseASE15"), sybaseAse);
		dialects.setEL(CommonUtil.createKey("SybaseASE157"), sybaseAse);
		String sybase = "org.hibernate.dialect.SybaseDialect";
		dialects.setEL(CommonUtil.createKey("Sybase11"), sybase);
		dialects.setEL(CommonUtil.createKey("SybaseAnywhere"), sybase);

		// HANA — Cloud/Column/Row variants gone, only HANADialect survives
		String hana = "org.hibernate.dialect.HANADialect";
		dialects.setEL(CommonUtil.createKey("HANACloudColumnStore"), hana);
		dialects.setEL(CommonUtil.createKey("HANAColumnStore"), hana);
		dialects.setEL(CommonUtil.createKey("HANARowStore"), hana);

		// DB2 — DB2390(V8) → DB2zDialect, DB2400(V7R3) → DB2iDialect, DB297 → DB2Dialect
		String db2 = "org.hibernate.dialect.DB2Dialect";
		String db2i = "org.hibernate.dialect.DB2iDialect";
		String db2z = "org.hibernate.dialect.DB2zDialect";
		dialects.setEL(CommonUtil.createKey("DB297"), db2);
		dialects.setEL(CommonUtil.createKey("DB2390"), db2z);
		dialects.setEL(CommonUtil.createKey("DB2390V8"), db2z);
		dialects.setEL(CommonUtil.createKey("DB2400"), db2i);
		dialects.setEL(CommonUtil.createKey("DB2400V7R3"), db2i);

		// Cockroach — CockroachDB192/201 gone, only CockroachDialect survives
		String cockroach = "org.hibernate.dialect.CockroachDialect";
		dialects.setEL(CommonUtil.createKey("CockroachDB192"), cockroach);
		dialects.setEL(CommonUtil.createKey("CockroachDB201"), cockroach);

	}

	/**
	 * Get the Hibernate dialect for the given Datasource
	 * 
	 * @param ds
	 *            - Datasource object to check dialect on
	 *
	 * @return the string dialect value, like "org.hibernate.dialect.PostgreSQLDialect"
	 */
	public static String getDialect(DataSource ds) {
		String name = ds.getClassDefinition().getClassName();
		if ("net.sourceforge.jtds.jdbc.Driver".equalsIgnoreCase(name)) {
			String dsn = ds.getConnectionStringTranslated();
			if (dsn.toLowerCase().indexOf("sybase") != -1) return getDialect("Sybase");
			return getDialect("SQLServer");
		}
		return getDialect(name);
	}

	/**
	 * Return a SQL dialect that match the given Name
	 *
	 * @param name
	 *            - Dialect name like "Oracle" or "MySQL57"
	 *
	 * @return the full dialect string name, like "org.hibernate.dialect.OracleDialect" or
	 *         "org.hibernate.dialect.MySQL57Dialect"
	 */
	public static String getDialect(String name) {
		if (Util.isEmpty(name))
			return null;
		String dialect = (String) dialects.get(CommonUtil.createKey(name), null);
		return dialect;
	}

	/**
	 * Get all configurable dialects
	 *
	 * @return the configured dialects
	 */
	public static Struct getDialects() {
		return dialects;
	}

	/**
	 * Get an iterator of dialect key names
	 *
	 * @return a String iteratator to iterate over all known dialects
	 */
	public static Iterator<String> getDialectNames() {
		return dialects.keysAsStringIterator();
	}
}
