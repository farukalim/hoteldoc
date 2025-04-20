package cn.itcast.hotel;

import cn.itcast.hotel.service.IHotelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: Faruk
 * @CreateTime: 2025-04-20  09:45
 * @Description: 测试HotelService类
 * @Version: 1.0
 */
@SpringBootTest
public class HotelServiceTests {

    @Autowired
    private IHotelService hotelService;

    @Test
    public void testContent() {
//        System.out.println(hotelService.filters(params));
    }
}
