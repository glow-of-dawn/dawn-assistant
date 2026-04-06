package com.dawn.plugin.mapper.ctemp;


import com.dawn.plugin.entity.ctemp.Temp;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Temp
 * 创建时间 2025-12-08 11:50:35
 *
 * @author bhyt2
 */
@Mapper
@Component
@ConditionalOnProperty(name = {"plugin-status.datasource-status"}, havingValue = "enable", matchIfMissing = true)
public interface TempMapper {

    /**
     * [Insert]
     *
     * @param temp []
     * @return int
     */
    @Insert("""
            INSERT INTO TEMP(ID, C1, C2, C3, C4, C5, C6, C7)
            VALUES(#{id}, #{c1}, #{c2}, #{c3}, #{c4}, #{c5}, #{c6}, #{c7})
            """)
    int create(Temp temp);

    /**
     * [Update]
     *
     * @param temp [Temp]
     * @return int
     */
    @Update("""
            UPDATE TEMP SET C1=#{c1}, C2=#{c2}, C3=#{c3}, C4=#{c4}, C5=#{c5}, C6=#{c6}, C7=#{c7}
            WHERE ID=#{id}
            """)
    int edit(Temp temp);

    /**
     * [Delete]
     *
     * @param id [主键]
     * @return int
     */
    @Delete("""
            DELETE FROM TEMP
            WHERE ID=#{id}
            """)
    int remove(@Param("id") Object id);

    /* -+-- select --+- */

    /**
     * [find by 主键]
     *
     * @param id [id]
     * @return Temp
     */
    @Select("""
            SELECT ID, C1, C2, C3, C4, C5, C6, C7
            FROM TEMP
            WHERE ID=#{id}
            """)
    Temp find(@Param("id") Object id);

    /**
     * [Select]
     *
     * @return List<Temp>
     */
    @Select("""
            SELECT ID, C1, C2, C3, C4, C5, C6, C7
            FROM TEMP
            """)
    List<Temp> findAll();

    /* -+-- others --+- */

    /**
     * [Update]
     *
     * @param id [id]
     * @param c1 [c1]
     * @param c2 [c2]
     * @return int
     */
    @Update("UPDATE TEMP SET C1=#{c1}, C2=#{c2} " +
            "WHERE ID=#{id} ")
    int editByC1(@Param("id") String id, @Param("c1") String c1, @Param("c2") String c2);

}
