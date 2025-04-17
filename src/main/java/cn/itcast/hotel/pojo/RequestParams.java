package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-17  14:06
 * @Description: 页面传递的参数
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestParams {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String brand;
    private String city;
    private Integer minPrice;
    private Integer maxPrice;
    private String starName;
    private String location;
}
