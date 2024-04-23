package com.project.alfa.utils

import kotlin.random.Random

private const val NUMS: String = "0123456789"
private const val UP_CHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
private const val LOW_CHARS: String = "abcdefghijklmnopqrstuvwxyz"
private const val SP_CHARS: String = "`~!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?"

class RandomGenerator {
    
    companion object {
        
        /**
         * 영문 대/소문자, 숫자 각각 최소 1개 이상으로 이루어진 랜덤 문자열 생성
         * length는 최소 3 이상, 3 미만일 경우 null 반환
         *
         * @param length - 길이
         * @return 생성된 문자열
         */
        fun randomString(length: Int): String? {
            if (length < 3) return null
            
            val sb = StringBuilder(length)
            
            var n: Int
            var u: Int
            var l: Int
            do {
                n = Random.nextInt(length - 1) + 1
                u = Random.nextInt(length - 1) + 1
                l = Random.nextInt(length - 1) + 1
            } while (n + u + l != length)
            
            while (n > 0 && n-- != 0) sb.append(NUMS[Random.nextInt(NUMS.length - 1)])
            while (u > 0 && u-- != 0) sb.append(UP_CHARS[Random.nextInt(UP_CHARS.length - 1)])
            while (l > 0 && l-- != 0) sb.append(LOW_CHARS[Random.nextInt(LOW_CHARS.length - 1)])
            
            return sb.toString().split("").shuffled().joinToString { "" }
        }
        
        /**
         * 영문 대/소문자, 숫자, 특수문자 각각 최소 1개 이상으로 이루어진 랜덤 문자열(임시 비밀번호) 생성
         * length는 최소 4 이상, 4 미만일 경우 null 반환
         *
         * @param length - 길이
         * @return 생성된 문자열
         */
        fun randomPassword(length: Int): String? {
            if (length < 4) return null
            
            val sb = StringBuilder(length)
            
            var n: Int
            var u: Int
            var l: Int
            var s: Int
            do {
                n = Random.nextInt(length - 1) + 1
                u = Random.nextInt(length - 1) + 1
                l = Random.nextInt(length - 1) + 1
                s = Random.nextInt(length - 1) + 1
            } while (n + u + l + s != length)
            
            while (n > 0 && n-- != 0) sb.append(NUMS[Random.nextInt(NUMS.length - 1)])
            while (u > 0 && u-- != 0) sb.append(UP_CHARS[Random.nextInt(UP_CHARS.length - 1)])
            while (l > 0 && l-- != 0) sb.append(LOW_CHARS[Random.nextInt(LOW_CHARS.length - 1)])
            while (s > 0 && s-- != 0) sb.append(SP_CHARS[Random.nextInt(SP_CHARS.length - 1)])
            
            return sb.toString().split("").shuffled().joinToString { "" }
        }
        
        fun randomNumber(min: Int, max: Int): Int = Random.nextInt(max - min + 1) + min
        
        fun randomHangul(length: Int): String {
            val sb = StringBuilder(length)
            for (i in 0 until length)
                sb.append(((Math.random() * 11172) + 0xAC00).toInt().toChar())
            return sb.toString()
        }
        
    }
    
}
