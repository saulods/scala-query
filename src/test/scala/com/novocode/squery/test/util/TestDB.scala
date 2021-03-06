package com.novocode.squery.test.util

import scala.collection.JavaConversions._
import java.io.{File, IOException, FileInputStream}
import java.util.Properties
import com.novocode.squery.combinator.extended.{ExtendedProfile, H2Driver, SQLiteDriver, PostgresDriver, MySQLDriver}
import com.novocode.squery.ResultSetInvoker
import com.novocode.squery.session._
import com.novocode.squery.session.Database.threadLocalSession
import com.novocode.squery.simple._
import com.novocode.squery.simple.StaticQueryBase._
import com.novocode.squery.simple.Implicit._

object TestDBOptions {
  val testDBDir = "test-dbs"
  lazy val dbProps = {
    val p = new Properties
    val in = new FileInputStream(new File(testDBDir, "databases.properties"))
    try { p.load(in) } finally { in.close() }
    p
  }
  def isEnabled(db: String) = "true" == dbProps.getProperty(db+".enabled")
  def get(db: String, o: String) = Option(dbProps.getProperty(db+"."+o))
}

abstract class TestDB {
  override def toString = url
  val url: String
  val jdbcDriver: String
  val driver: ExtendedProfile
  def dbName = ""
  def userName = ""
  def createDB() = Database.forURL(url, driver = jdbcDriver)
  def cleanUpBefore() = cleanUp()
  def cleanUpAfter() = cleanUp()
  def cleanUp() {}
  def deleteDBFiles(prefix: String) {
    val dir = new File(TestDBOptions.testDBDir)
    if(!dir.isDirectory) throw new IOException("Directory "+TestDBOptions.testDBDir+" not found")
    for(f <- dir.listFiles if f.getName startsWith prefix) {
      val p = TestDBOptions.testDBDir+"/"+f.getName
      if(f.delete) println("[Deleted database file "+p+"]")
      else throw new IOException("Couldn't delete database file "+p)
    }
  }
  def isEnabled = true
  def getLocalTables(implicit session: Session): List[String] = {
    val tables = ResultSetInvoker[(String,String,String)](_.conn.getMetaData().getTables("", "", null, null))
    tables.list(())(session).map(_._3.toLowerCase)
  }
}

class SQLiteTestDB(dburl: String) extends TestDB {
  val url = dburl
  val jdbcDriver = "org.sqlite.JDBC"
  val driver = SQLiteDriver
  override def getLocalTables(implicit session: Session) =
    super.getLocalTables(session).filter(s => !s.contains("sqlite_")).sortBy(identity)
}

class ExternalTestDB(confName: String, val driver: ExtendedProfile) extends TestDB {
  val jdbcDriver = TestDBOptions.get(confName, "driver").orNull
  val urlTemplate = TestDBOptions.get(confName, "url").getOrElse("")
  override def dbName = TestDBOptions.get(confName, "testDB").getOrElse("")
  val url = urlTemplate.replace("[DB]", dbName)
  val configuredUserName = TestDBOptions.get(confName, "user").orNull
  val password = TestDBOptions.get(confName, "password").orNull
  override def userName = TestDBOptions.get(confName, "user").orNull

  val adminDBURL = urlTemplate.replace("[DB]", TestDBOptions.get(confName, "adminDB").getOrElse(""))
  val create = TestDBOptions.get(confName, "create").getOrElse("").replace("[DB]", dbName)
  val drop = TestDBOptions.get(confName, "drop").getOrElse("").replace("[DB]", dbName)

  override def isEnabled = TestDBOptions.isEnabled(confName)

  override def createDB() = Database.forURL(url, driver = jdbcDriver, user = configuredUserName, password = password)

  override def cleanUpBefore() {
    if(drop.length > 0 || create.length > 0) {
      println("[Creating test database "+this+"]")
      Database.forURL(adminDBURL, driver = jdbcDriver, user = configuredUserName, password = password) withSession {
        updateNA(drop).execute
        updateNA(create).execute
      }
    }
  }

  override def cleanUpAfter() {
    if(drop.length > 0) {
      println("[Dropping test database "+this+"]")
      Database.forURL(adminDBURL, driver = jdbcDriver, user = configuredUserName, password = password) withSession {
        updateNA(drop).execute
      }
    }
  }
}

object TestDB {
  type TestDBSpec = (DBTestObject => TestDB)

  def H2Mem(to: DBTestObject) = new TestDB {
    val url = "jdbc:h2:mem:test1"
    val jdbcDriver = "org.h2.Driver"
    val driver = H2Driver
    override val dbName = "test1"
  }

  def H2Disk(to: DBTestObject) = new TestDB {
    override val dbName = "h2-"+to.testClassName
    val url = "jdbc:h2:./"+TestDBOptions.testDBDir+"/"+dbName
    val jdbcDriver = "org.h2.Driver"
    val driver = H2Driver
    override def cleanUp() = deleteDBFiles(dbName)
  }

  def SQLiteMem(to: DBTestObject) = new SQLiteTestDB("jdbc:sqlite::memory:") {
    override val dbName = ":memory:"
  }

  def SQLiteDisk(to: DBTestObject) = {
    val prefix = "sqlite-"+to.testClassName
    new SQLiteTestDB("jdbc:sqlite:./"+TestDBOptions.testDBDir+"/"+prefix+".db") {
      override val dbName = prefix
      override def cleanUp() = deleteDBFiles(prefix)
    }
  }

  def Postgres(to: DBTestObject) = new ExternalTestDB("postgres", PostgresDriver) {
    override def getLocalTables(implicit session: Session) = {
      val tables = ResultSetInvoker[(String,String,String)](_.conn.getMetaData().getTables("", "public", null, null))
      tables.list(())(session).map(_._3.toLowerCase).filter(s => !s.endsWith("_pkey") && !s.endsWith("_id_seq"))
    }
  }

  def MySQL(to: DBTestObject) = new ExternalTestDB("mysql", MySQLDriver) {
    override def userName = super.userName + "@localhost"
  }
}
