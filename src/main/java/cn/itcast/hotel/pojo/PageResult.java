package cn.itcast.hotel.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-17  14:08
 * @Description: 返回到页面的数据
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
}
