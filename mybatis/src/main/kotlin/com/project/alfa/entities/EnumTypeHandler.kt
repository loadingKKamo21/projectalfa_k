package com.project.alfa.entities

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

class EnumTypeHandler<E : Enum<E>>(type: Class<E>) : BaseTypeHandler<E>() {
    
    private val type: Class<E> = type ?: throw IllegalArgumentException("Type argument cannot be null.")
    
    @Throws(SQLException::class)
    override fun setNonNullParameter(ps: PreparedStatement?, i: Int, parameter: E, jdbcType: JdbcType?) = ps!!.setInt(i,
                                                                                                                      parameter.ordinal)
    
    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet?, columnName: String?): E = getEnum(rs!!.getInt(columnName))
    
    @Throws(SQLException::class)
    override fun getNullableResult(rs: ResultSet?, columnIndex: Int): E = getEnum(rs!!.getInt(columnIndex))
    
    @Throws(SQLException::class)
    override fun getNullableResult(cs: CallableStatement?, columnIndex: Int): E = getEnum(cs!!.getInt(columnIndex))
    
    private fun getEnum(ordinal: Int): E {
        try {
            return type.enumConstants[ordinal]
        } catch (e: Exception) {
            throw IllegalArgumentException("Cannot convert $ordinal to ${type.simpleName} by ordinal value.", e)
        }
    }
    
}