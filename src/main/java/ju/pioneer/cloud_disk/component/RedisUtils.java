package ju.pioneer.cloud_disk.component;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis通用工具类
 *
 * @param <V> Redis Value泛型
 */
@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 判断key是否存在
     *
     * @param key Redis key
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        if (key == null) {
            return false;
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("判断redis key[{}]是否存在异常", key, e);
            return false;
        }
    }

    /**
     * 设置key过期时间
     *
     * @param time 时长
     * @param unit 时间单位
     */
    public boolean expire(String key, long time, TimeUnit unit) {
        try {
            if (time > 0) {
                return redisTemplate.expire(key, time, unit);
            }
            return false;
        } catch (Exception e) {
            logger.error("设置key[{}]过期时间异常", key, e);
            return false;
        }
    }

    /**
     * 设置key过期时间，单位秒
     *
     * @param key     Redis key
     * @param seconds 时长
     * @return 是否设置成功
     */
    public boolean expireSecond(String key, long seconds) {
        return expire(key, seconds, TimeUnit.SECONDS);
    }

    /**
     * 获取key剩余过期时间（秒）
     *
     * @return -1永久有效 / -2key不存在 / >0剩余秒数
     */
    public Long getExpireSecond(String key) {
        if (!hasKey(key)) {
            return -2L;
        }
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 删除单个/批量key
     *
     * @param keys 可变参数key数组
     */
    public void delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        try {
            Collection<String> keyList = Arrays.asList(keys);
            redisTemplate.delete(keyList);
        } catch (Exception e) {
            logger.error("删除redis keys异常,keys:{}", Arrays.toString(keys), e);
        }
    }

    /**
     * 获取缓存值
     *
     * @param key Redis key
     * @return 缓存值
     */
    public V get(String key) {
        if (key == null) {
            return null;
        }
        try {
            ValueOperations<String, V> valueOps = redisTemplate.opsForValue();
            return valueOps.get(key);
        } catch (Exception e) {
            logger.error("获取redis key[{}]值异常", key, e);
            return null;
        }
    }

    /**
     * 存入无过期缓存
     */
    public boolean set(String key, V value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置redis key:{},value:{}失败", key, value, e);
            return false;
        }
    }

    /**
     * 存入带过期时间缓存（单位秒）
     *
     * @param time <=0 永久有效
     */
    public boolean setExpireTime(String key, V value, long time) {
        try {
            ValueOperations<String, V> valueOps = redisTemplate.opsForValue();
            if (time > 0) {
                valueOps.set(key, value, time, TimeUnit.MINUTES);
            } else {
                valueOps.set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("setex redis key:{},value:{},time:{}失败", key, value, time, e);
            return false;
        }
    }

    /**
     * 仅当key不存在时设置（分布式锁常用）
     *
     * @return true 设置成功；false key已存在
     */
    public Boolean setIfAbsent(String key, V value, long time, TimeUnit unit) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, time, unit);
        } catch (Exception e) {
            logger.error("setIfAbsent key[{}]异常", key, e);
            return false;
        }
    }

    /**
     * 数值自增
     *
     * @param delta 自增步长
     * @return 自增后值
     */
    public Long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("自增步长不能为负数");
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            logger.error("key[{}]自增异常", key, e);
            return null;
        }
    }

    /**
     * 数值自减
     *
     * @param key     Redis key
     * @param delta 自减步长
     * @return 自减后值
     */
    public Long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("自减步长不能为负数");
        }
        try {
            return redisTemplate.opsForValue().decrement(key, delta);
        } catch (Exception e) {
            logger.error("key[{}]自减异常", key, e);
            return null;
        }
    }
}