package com.fusemi.dashboard.config;

import com.fusemi.dashboard.entity.*;
import com.fusemi.dashboard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SalesDataRepository salesDataRepository;
    private final SalesShipmentRepository shipmentRepository;
    private final SalesOrderRepository orderRepository;
    private final SalesInvoiceRepository invoiceRepository;
    private final SalesDealRepository dealRepository;
    private final SalesCustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           SalesDataRepository salesDataRepository,
                           SalesShipmentRepository shipmentRepository,
                           SalesOrderRepository orderRepository,
                           SalesInvoiceRepository invoiceRepository,
                           SalesDealRepository dealRepository,
                           SalesCustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.salesDataRepository = salesDataRepository;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 创建默认角色
        if (roleRepository.count() == 0) {
            Role superAdmin = new Role();
            superAdmin.setName("超级管理员");
            superAdmin.setCode("R_SUPER");
            superAdmin.setDescription("系统超级管理员，拥有所有权限");
            superAdmin.setStatus(0);
            roleRepository.save(superAdmin);

            Role admin = new Role();
            admin.setName("管理员");
            admin.setCode("R_ADMIN");
            admin.setDescription("系统管理员");
            admin.setStatus(0);
            roleRepository.save(admin);

            Role user = new Role();
            user.setName("普通用户");
            user.setCode("R_USER");
            user.setDescription("普通用户");
            user.setStatus(0);
            roleRepository.save(user);
        }

        // 创建默认管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setEmail("admin@fusemi.com");
            admin.setStatus(0);

            roleRepository.findByCode("R_SUPER").ifPresent(role ->
                admin.setRoles(Set.of(role))
            );
            userRepository.save(admin);
        }

        // 初始化业务数据（如果为空）
        if (salesDataRepository.count() == 0) {
            initBusinessData();
        }
    }

    private void initBusinessData() {
        LocalDate today = LocalDate.now();
        String[] regions = {"华东", "华南", "华北", "西南", "西北"};
        String[] salespersons = {"张三", "李四", "王五", "赵六", "钱七"};
        String[] products = {"SC43N12TFI8_C", "SF150R12A6H", "S3L450R12D6L_C20", "SC19N07TFI8_B", "SC28N12TFI8_C"};
        String[] customers = {"华为科技", "中兴通讯", "小米集团", "OPPO移动", "vivo移动", "联想集团", "海尔电器", "格力电器"};
        String[] channels = {"线上", "线下", "渠道"};
        String[] stages = {"意向", "洽谈", "签约"};
        String[] carriers = {"顺丰速运", "中通快递", "圆通速递", "韵达快递"};

        // 生成销售数据
        for (int i = 0; i < 50; i++) {
            SalesData data = new SalesData();
            data.setSaleDate(today.minusDays(random.nextInt(30)));
            data.setRegion(regions[random.nextInt(regions.length)]);
            data.setSalesperson(salespersons[random.nextInt(salespersons.length)]);
            data.setProductName(products[random.nextInt(products.length)]);
            data.setAmount(new BigDecimal(10000 + random.nextInt(90000)));
            data.setQuantity(10 + random.nextInt(90));
            data.setUnitPrice(new BigDecimal(data.getAmount().intValue() / data.getQuantity()));
            data.setCustomerName(customers[random.nextInt(customers.length)]);
            data.setOrderNo("SO" + System.currentTimeMillis() + random.nextInt(1000));
            data.setStatus(random.nextInt(3) == 0 ? "cancelled" : (random.nextInt(2) == 0 ? "pending" : "已完成"));
            salesDataRepository.save(data);
        }

        // 生成订单数据
        for (int i = 0; i < 50; i++) {
            SalesOrder order = new SalesOrder();
            order.setOrderNo("ORD" + System.currentTimeMillis() + random.nextInt(1000));
            order.setOrderDate(today.minusDays(random.nextInt(30)));
            order.setCustomerName(customers[random.nextInt(customers.length)]);
            order.setSalesperson(salespersons[random.nextInt(salespersons.length)]);
            order.setSource(channels[random.nextInt(channels.length)]);
            order.setAmount(new BigDecimal(5000 + random.nextInt(95000)));
            order.setStatus(random.nextInt(4)); // 0=pending, 1=confirmed, 2=shipped, 3=completed
            orderRepository.save(order);
        }

        // 生成发货数据
        for (int i = 0; i < 40; i++) {
            SalesShipment shipment = new SalesShipment();
            shipment.setShipmentNo("SHP" + System.currentTimeMillis() + random.nextInt(1000));
            shipment.setOrderNo("ORD" + System.currentTimeMillis() + random.nextInt(1000));
            shipment.setShipmentDate(today.minusDays(random.nextInt(30)));
            shipment.setCarrier(carriers[random.nextInt(carriers.length)]);
            shipment.setStatus(random.nextInt(3) == 0 ? "pending" : (random.nextInt(2) == 0 ? "in_transit" : "delivered"));
            shipment.setAmount(new BigDecimal(5000 + random.nextInt(45000)));
            shipment.setReceiverAddress("广东省深圳市南山区科技路" + (100 + random.nextInt(900)) + "号");
            shipment.setReceiverPhone("138" + (10000000 + random.nextInt(90000000)));
            shipmentRepository.save(shipment);
        }

        // 生成发票数据
        for (int i = 0; i < 35; i++) {
            SalesInvoice invoice = new SalesInvoice();
            invoice.setInvoiceNo("INV" + System.currentTimeMillis() + random.nextInt(1000));
            invoice.setOrderNo("ORD" + System.currentTimeMillis() + random.nextInt(1000));
            invoice.setCustomerName(customers[random.nextInt(customers.length)]);
            invoice.setInvoiceDate(today.minusDays(random.nextInt(30)));
            invoice.setType(random.nextInt(2) == 0 ? "专用发票" : "普通发票");
            invoice.setAmount(new BigDecimal(3000 + random.nextInt(47000)));
            invoice.setStatus(random.nextInt(3)); // 0=未开, 1=已开, 2=已收
            invoiceRepository.save(invoice);
        }

        // 生成成交数据
        for (int i = 0; i < 45; i++) {
            SalesDeal deal = new SalesDeal();
            deal.setDealDate(today.minusDays(random.nextInt(30)));
            deal.setProductName(products[random.nextInt(products.length)]);
            deal.setAmount(new BigDecimal(8000 + random.nextInt(92000)));
            deal.setQuantity(5 + random.nextInt(45));
            deal.setConversionRate(new BigDecimal(60 + random.nextInt(40)));
            deal.setStage(stages[random.nextInt(stages.length)]);
            dealRepository.save(deal);
        }

        // 生成客户数据
        for (int i = 0; i < 30; i++) {
            SalesCustomer customer = new SalesCustomer();
            customer.setName(customers[random.nextInt(customers.length)] + "分公司");
            customer.setPhone("138" + (10000000 + random.nextInt(90000000)));
            customer.setEmail(customer.getName().substring(0, 2) + "@company.com");
            customer.setChannel(channels[random.nextInt(channels.length)]);
            customer.setIsNew(random.nextInt(2)); // 0=老客户, 1=新客户
            customer.setFirstPurchaseDate(today.minusDays(random.nextInt(365)));
            customer.setRepeatPurchaseCount(random.nextInt(10));
            customer.setLevel(random.nextInt(3) == 0 ? "VIP" : "普通");
            customerRepository.save(customer);
        }
    }
}
