package com.kent.workflow.node.action.transfer.source.io

import java.sql._

import com.kent.pub.db.{Column, DBLink, OracleOperator}
import com.kent.workflow.node.action.transfer.source.Source

class OracleSource(dbLink: DBLink,  tableSql: String)  extends Source {
  implicit var conn: Connection = _
  var query: String = _
  var stat: PreparedStatement = _
  var rs: ResultSet = _
  
  def init(): Unit = {
    val parseTable = if(tableSql.trim().contains(" ")) s"($tableSql)" else tableSql
    query = s"select * from $parseTable AAA_BBB_CCC"
    conn = OracleOperator.getConnection(dbLink)
    stat = conn.prepareStatement(query)
    rs = stat.executeQuery()
  }

  def fillRowBuffer(): List[List[String]] = {
    val rowsBuffer = scala.collection.mutable.ArrayBuffer[List[String]]()
    var cnt = 0
    while (cnt < ROW_MAX_SIZE && rs.next()) {
      cnt += 1
      val row = (1 to this.colNum).map(rs.getString(_)).map { x => if(x != null) x.replaceAll("(\n|\r)+", " ") else x }.toList
      rowsBuffer.append(row)
    }
    //结尾
    if(cnt < ROW_MAX_SIZE){
      isEnd = true
    }
    rowsBuffer.toList
  }

  def getColNums: Option[List[Column]] = {
    val sql = s"$query where 1 < 0"
    Some(OracleOperator.getColumns(sql, dbLink))
  }

  def finish(): Unit = {
    if(rs != null) rs.close()
    if(stat != null) stat.close()
    if(conn != null) conn.close()
  }
}