package com.novocode.squery.test

import java.sql._
import java.io.PrintWriter
import scala.Array
import org.junit.Test
import org.junit.Assert._
import com.novocode.squery.ResultSetInvoker
import com.novocode.squery.combinator._
import com.novocode.squery.combinator.TypeMapper._
import com.novocode.squery.combinator.basic.{BasicTable => Table}
import com.novocode.squery.combinator.extended.{H2Driver, PostgresDriver}
import com.novocode.squery.meta._
import com.novocode.squery.session._
import com.novocode.squery.session.Database.threadLocalSession
import com.novocode.squery.simple.StaticQueryBase.updateNA
import com.novocode.squery.simple.Implicit._
import com.novocode.squery.test.util._
import com.novocode.squery.test.util.TestDB._

object MetaTest extends DBTestObject(H2Mem, Postgres, MySQL)

class MetaTest(tdb: TestDB) extends DBTest(tdb) {
  import tdb.driver.Implicit._

  object Users extends Table[(Int, String, Option[String])]("users") {
    def id = column[Int]("id", O NotNull, O PrimaryKey)
    def first = column[String]("first", O Default "NFN", O DBType "varchar(64)")
    def last = column[Option[String]]("last")
    def * = id ~ first ~ last
  }

  object Orders extends Table[(Int, Int, String, Boolean, Option[Boolean])]("orders") {
    def userID = column[Int]("userID", O NotNull)
    def orderID = column[Int]("orderID", O NotNull, O PrimaryKey)
    def product = column[String]("product")
    def shipped = column[Boolean]("shipped", O Default false, O NotNull)
    def rebate = column[Option[Boolean]]("rebate", O Default Some(false))
    def * = userID ~ orderID ~ product ~ shipped ~ rebate
    def userFK = foreignKey("user_fk", userID, Users)(_.id)
  }

  @Test def test() {

    db withSession {

      val ddl = (Users.ddl ++ Orders.ddl)
      println("DDL used to create tables:")
      for(s <- ddl.createStatements) println("  "+s)
      ddl.create

      println("Type info from DatabaseMetaData:")
      for(t <- MTypeInfo.getTypeInfo) println("  "+t)

      assertTrue(Set("ORDERS", "USERS") subsetOf MTable.getTables.list.map(_.name.name.toUpperCase).toSet)

      if(tdb.driver != H2Driver && tdb.driver != PostgresDriver) { // Not supported by H2 and PostgreSQL
        println("Functions from DatabaseMetaData:")
        for(f <- MFunction.getFunctions(MQName.local("%"))) {
          println("  "+f)
          for(c <- f.getFunctionColumns()) println("    "+c)
        }
      }

      println("UDTs from DatabaseMetaData:")
      for(u <- MUDT.getUDTs(MQName.local("%"))) println("  "+u)

      println("Procedures from DatabaseMetaData:")
      MProcedure.getProcedures(MQName.local("%")).foreach({ p =>
        println("  "+p)
        for(c <- p.getProcedureColumns()) println("    "+c)
      }, 3)

      println("Tables from DatabaseMetaData:")
      for(t <- MTable.getTables.list if Set("USERS", "ORDERS") contains t.name.name.toUpperCase) {
        println("  "+t)
        for(c <- t.getColumns) {
          println("    "+c)
          for(p <- c.getColumnPrivileges) println("      "+p)
        }
        for(v <- t.getVersionColumns) println("    "+v)
        for(k <- t.getPrimaryKeys) println("    "+k)
        for(k <- t.getImportedKeys) println("    Imported "+k)
        for(k <- t.getExportedKeys) println("    Exported "+k)
        for(i <- t.getIndexInfo()) println("    "+i)
        for(p <- t.getTablePrivileges) println("    "+p)
        for(c <- t.getBestRowIdentifier(MBestRowIdentifierColumn.Scope.Session))
          println("    Row identifier for session: "+c)
      }

      println("Schemas from DatabaseMetaData:")
      for(t <- MSchema.getSchemas) println("  "+t)

      if(tdb.driver != H2Driver) { // Not supported by H2
        println("Client Info Properties from DatabaseMetaData:")
        for(t <- MClientInfoProperty.getClientInfoProperties) println("  "+t)
      }

      println("Generated code:")
      val out = new PrintWriter(System.out)
      for(t <- MTable.getTables.list if Set("USERS", "ORDERS") contains t.name.name.toUpperCase)
        CodeGen.output(t, out)
      out.flush

      for(t <- tdb.getLocalTables) updateNA("drop table " + t).execute
      assertTrue(Set("ORDERS", "USERS") intersect MTable.getTables.list.map(_.name.name.toUpperCase).toSet isEmpty)
    }
  }
}
