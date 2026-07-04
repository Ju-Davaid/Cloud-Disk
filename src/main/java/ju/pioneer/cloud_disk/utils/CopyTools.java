package ju.pioneer.cloud_disk.utils;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CopyTools {
    /**
     * 集合对象拷贝 S -> T
     *
     * @param list   源集合
     * @param tClass 目标类Class
     * @return 拷贝后的集合
     */
    public static <T, S> List<T> copyList(List<S> list, Class<T> tClass) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .map(source -> {
                    try {
                        T target = tClass.getDeclaredConstructor().newInstance();
                        BeanUtils.copyProperties(source, target);
                        return target;
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        throw new RuntimeException("对象拷贝失败", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public static <T, S> T copy(S source, Class<T> tClass) {
        try {
            T target = tClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            e.fillInStackTrace();
            throw new RuntimeException("对象拷贝失败", e);
        }
    }

}
