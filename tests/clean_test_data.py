#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
清理测试数据脚本：删除超过3次面试的记录
"""

import sys
import os
import mysql.connector
from datetime import datetime

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.config import config

def clean_excess_interview_records():
    """
    清理超过3次面试的记录
    保留每个候选人的前3次面试记录
    """
    print("=" * 60)
    print("清理测试数据：删除超过3次面试的记录")
    print("=" * 60)
    print()
    
    # 建立数据库连接
    try:
        conn = mysql.connector.connect(
            host=config.DB_HOST,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            database=config.DB_NAME
        )
        cursor = conn.cursor(dictionary=True)
        
        print(f"成功连接到数据库: {config.DB_NAME}")
        print()
        
        # 查询所有简历的面试次数
        query = """
        SELECT 
            resume_id,
            COUNT(*) as interview_count
        FROM interview_record
        GROUP BY resume_id
        HAVING COUNT(*) > 3
        ORDER BY interview_count DESC
        """
        
        cursor.execute(query)
        excess_records = cursor.fetchall()
        
        if not excess_records:
            print("✅ 没有发现超过3次面试的记录")
            return
        
        print(f"发现 {len(excess_records)} 个简历的面试记录超过3次：")
        print()
        
        # 显示每个简历的面试次数
        for record in excess_records:
            resume_id = record['resume_id']
            interview_count = record['interview_count']
            print(f"  简历ID {resume_id}: {interview_count}次面试")
        
        print()
        print("=" * 60)
        print("开始清理...")
        print("=" * 60)
        print()
        
        # 对每个简历，只保留前3次面试记录
        total_deleted = 0
        for record in excess_records:
            resume_id = record['resume_id']
            interview_count = record['interview_count']
            
            # 查询该简历的所有面试记录（按时间排序）
            query = """
            SELECT interview_record_id, interview_time, interview_round
            FROM interview_record
            WHERE resume_id = %s
            ORDER BY create_time ASC
            """
            
            cursor.execute(query, (resume_id,))
            interviews = cursor.fetchall()
            
            if len(interviews) > 3:
                # 删除第4次及以后的面试记录
                to_delete = interviews[3:]
                
                for interview in to_delete:
                    interview_id = interview['interview_record_id']
                    interview_time = interview['interview_time']
                    interview_round = interview['interview_round']
                    
                    # 删除面试记录
                    delete_query = "DELETE FROM interview_record WHERE interview_record_id = %s"
                    cursor.execute(delete_query, (interview_id,))
                    
                    total_deleted += 1
                    print(f"  ✅ 删除面试记录 {interview_id} (简历ID: {resume_id}, 轮次: {interview_round}, 时间: {interview_time})")
        
        # 提交事务
        conn.commit()
        
        print()
        print("=" * 60)
        print(f"清理完成！共删除 {total_deleted} 条面试记录")
        print("=" * 60)
        print()
        
        # 验证清理结果
        print("验证清理结果...")
        cursor.execute(query)
        remaining_excess = cursor.fetchall()
        
        if remaining_excess:
            print(f"⚠️  仍有 {len(remaining_excess)} 个简历的面试记录超过3次")
        else:
            print("✅ 所有简历的面试记录都已符合要求（最多3次）")
        
        # 显示清理后的统计信息
        cursor.execute("SELECT COUNT(*) as total FROM interview_record")
        total_count = cursor.fetchone()
        print(f"当前面试记录总数: {total_count['total']}")
        
        # 关闭连接
        cursor.close()
        conn.close()
        
    except mysql.connector.Error as e:
        print(f"❌ 数据库错误: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

def show_interview_statistics():
    """
    显示面试统计信息
    """
    print("=" * 60)
    print("面试统计信息")
    print("=" * 60)
    print()
    
    try:
        conn = mysql.connector.connect(
            host=config.DB_HOST,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            database=config.DB_NAME
        )
        cursor = conn.cursor(dictionary=True)
        
        # 总面试记录数
        cursor.execute("SELECT COUNT(*) as total FROM interview_record")
        total_count = cursor.fetchone()
        print(f"总面试记录数: {total_count['total']}")
        
        # 按轮次统计
        cursor.execute("""
        SELECT 
            interview_round,
            COUNT(*) as count
        FROM interview_record
        GROUP BY interview_round
        ORDER BY count DESC
        """)
        round_stats = cursor.fetchall()
        print()
        print("按面试轮次统计:")
        for stat in round_stats:
            round_name = stat['interview_round']
            count = stat['count']
            round_map = {
                'FIRST_ROUND': '一面',
                'SECOND_ROUND': '二面',
                'THIRD_ROUND': '三面'
            }
            round_display = round_map.get(round_name, round_name)
            print(f"  {round_display}: {count}次")
        
        # 按结果统计
        cursor.execute("""
        SELECT 
            interview_result,
            COUNT(*) as count
        FROM interview_record
        WHERE interview_result IS NOT NULL
        GROUP BY interview_result
        ORDER BY count DESC
        """)
        result_stats = cursor.fetchall()
        print()
        print("按面试结果统计:")
        for stat in result_stats:
            result = stat['interview_result']
            count = stat['count']
            result_map = {
                'PASSED': '通过',
                'FAILED': '不通过'
            }
            result_display = result_map.get(result, result)
            print(f"  {result_display}: {count}次")
        
        # 超过3次的简历
        cursor.execute("""
        SELECT 
            resume_id,
            COUNT(*) as interview_count
        FROM interview_record
        GROUP BY resume_id
        HAVING COUNT(*) > 3
        ORDER BY interview_count DESC
        """)
        excess_records = cursor.fetchall()
        print()
        print(f"超过3次面试的简历数: {len(excess_records)}")
        
        cursor.close()
        conn.close()
        
    except mysql.connector.Error as e:
        print(f"❌ 数据库错误: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ 发生错误: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description="清理测试数据脚本")
    parser.add_argument("--clean", action="store_true", help="清理超过3次面试的记录")
    parser.add_argument("--stats", action="store_true", help="显示面试统计信息")
    parser.add_argument("--all", action="store_true", help="清理并显示统计信息")
    
    args = parser.parse_args()
    
    if args.all:
        clean_excess_interview_records()
        print()
        show_interview_statistics()
    elif args.clean:
        clean_excess_interview_records()
    elif args.stats:
        show_interview_statistics()
    else:
        print("使用方法:")
        print("  python clean_test_data.py --clean   # 清理超过3次面试的记录")
        print("  python clean_test_data.py --stats    # 显示面试统计信息")
        print("  python clean_test_data.py --all     # 清理并显示统计信息")