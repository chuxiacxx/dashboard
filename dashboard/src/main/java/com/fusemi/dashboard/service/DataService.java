package com.fusemi.dashboard.service;

import com.fusemi.dashboard.common.PageResult;
import com.fusemi.dashboard.entity.*;
import com.fusemi.dashboard.repository.*;
import com.fusemi.dashboard.vo.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务数据服务
 */
@Service
public class DataService {

    private final SalesDataRepository salesDataRepository;
    private final SalesShipmentRepository shipmentRepository;
    private final SalesOrderRepository orderRepository;
    private final SalesInvoiceRepository invoiceRepository;

    private final SalesDealRepository dealRepository;
    private final SalesCustomerRepository customerRepository;

    public DataService(SalesDataRepository salesDataRepository,
                      SalesShipmentRepository shipmentRepository,
                      SalesOrderRepository orderRepository,
                      SalesInvoiceRepository invoiceRepository,
                      SalesDealRepository dealRepository,
                      SalesCustomerRepository customerRepository) {
        this.salesDataRepository = salesDataRepository;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
    }

    // ==================== 销售数据 ====================

    public PageResult<SalesDataVO> getSalesPage(Integer current, Integer size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesData> page = salesDataRepository.findAll(pageRequest);

        List<SalesDataVO> records = page.getContent().stream()
                .filter(d -> {
                    if (startDate != null && endDate != null) {
                        return d.getSaleDate() != null &&
                               !d.getSaleDate().isBefore(startDate) &&
                               !d.getSaleDate().isAfter(endDate);
                    }
                    return true;
                })
                .map(this::toSalesDataVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesDataVO toSalesDataVO(SalesData d) {
        SalesDataVO vo = new SalesDataVO();
        vo.setId(d.getId());
        vo.setSaleDate(d.getSaleDate());
        vo.setRegion(d.getRegion());
        vo.setSalesperson(d.getSalesperson());
        vo.setProductName(d.getProductName());
        vo.setAmount(d.getAmount());
        vo.setQuantity(d.getQuantity());
        vo.setUnitPrice(d.getUnitPrice());
        vo.setCustomerName(d.getCustomerName());
        vo.setOrderNo(d.getOrderNo());
        vo.setStatus(d.getStatus());
        return vo;
    }

    // ==================== 发货数据 ====================

    public PageResult<SalesShipmentVO> getShipmentPage(Integer current, Integer size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesShipment> page = shipmentRepository.findAll(pageRequest);

        List<SalesShipmentVO> records = page.getContent().stream()
                .filter(s -> {
                    if (startDate != null && endDate != null) {
                        return s.getShipmentDate() != null &&
                               !s.getShipmentDate().isBefore(startDate) &&
                               !s.getShipmentDate().isAfter(endDate);
                    }
                    return true;
                })
                .map(this::toShipmentVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesShipmentVO toShipmentVO(SalesShipment s) {
        SalesShipmentVO vo = new SalesShipmentVO();
        vo.setId(s.getId());
        vo.setShipmentNo(s.getShipmentNo());
        vo.setOrderNo(s.getOrderNo());
        vo.setShipmentDate(s.getShipmentDate());
        vo.setCarrier(s.getCarrier());
        vo.setStatus(s.getStatus());
        vo.setAmount(s.getAmount());
        vo.setReceiverAddress(s.getReceiverAddress());
        vo.setReceiverPhone(s.getReceiverPhone());
        return vo;
    }

    // ==================== 订单数据 ====================

    public PageResult<SalesOrderVO> getOrderPage(Integer current, Integer size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesOrder> page = orderRepository.findAll(pageRequest);

        List<SalesOrderVO> records = page.getContent().stream()
                .filter(o -> {
                    if (startDate != null && endDate != null) {
                        return o.getOrderDate() != null &&
                               !o.getOrderDate().isBefore(startDate) &&
                               !o.getOrderDate().isAfter(endDate);
                    }
                    return true;
                })
                .map(this::toOrderVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesOrderVO toOrderVO(SalesOrder o) {
        SalesOrderVO vo = new SalesOrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setOrderDate(o.getOrderDate());
        vo.setCustomerName(o.getCustomerName());
        vo.setSalesperson(o.getSalesperson());
        vo.setSource(o.getSource());
        vo.setAmount(o.getAmount());
        vo.setStatus(o.getStatus());
        return vo;
    }

    // ==================== 发票数据 ====================

    public PageResult<SalesInvoiceVO> getInvoicePage(Integer current, Integer size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesInvoice> page = invoiceRepository.findAll(pageRequest);

        List<SalesInvoiceVO> records = page.getContent().stream()
                .filter(i -> {
                    if (startDate != null && endDate != null) {
                        return i.getInvoiceDate() != null &&
                               !i.getInvoiceDate().isBefore(startDate) &&
                               !i.getInvoiceDate().isAfter(endDate);
                    }
                    return true;
                })
                .map(this::toInvoiceVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesInvoiceVO toInvoiceVO(SalesInvoice i) {
        SalesInvoiceVO vo = new SalesInvoiceVO();
        vo.setId(i.getId());
        vo.setInvoiceNo(i.getInvoiceNo());
        vo.setOrderNo(i.getOrderNo());
        vo.setCustomerName(i.getCustomerName());
        vo.setInvoiceDate(i.getInvoiceDate());
        vo.setType(i.getType());
        vo.setAmount(i.getAmount());
        vo.setStatus(i.getStatus());
        return vo;
    }

    // ==================== 成交数据 ====================

    public PageResult<SalesDealVO> getDealPage(Integer current, Integer size, LocalDate startDate, LocalDate endDate) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesDeal> page = dealRepository.findAll(pageRequest);

        List<SalesDealVO> records = page.getContent().stream()
                .filter(d -> {
                    if (startDate != null && endDate != null) {
                        return d.getDealDate() != null &&
                               !d.getDealDate().isBefore(startDate) &&
                               !d.getDealDate().isAfter(endDate);
                    }
                    return true;
                })
                .map(this::toDealVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesDealVO toDealVO(SalesDeal d) {
        SalesDealVO vo = new SalesDealVO();
        vo.setId(d.getId());
        vo.setDealDate(d.getDealDate());
        vo.setProductName(d.getProductName());
        vo.setAmount(d.getAmount());
        vo.setQuantity(d.getQuantity());
        vo.setConversionRate(d.getConversionRate());
        vo.setStage(d.getStage());
        return vo;
    }

    // ==================== 客户数据 ====================

    public PageResult<SalesCustomerVO> getCustomerPage(Integer current, Integer size, String channel, Integer isNew) {
        PageRequest pageRequest = PageRequest.of(current - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<SalesCustomer> page = customerRepository.findAll(pageRequest);

        List<SalesCustomerVO> records = page.getContent().stream()
                .filter(c -> {
                    if (channel != null && !channel.isEmpty() && !channel.equals(c.getChannel())) return false;
                    if (isNew != null && !isNew.equals(c.getIsNew())) return false;
                    return true;
                })
                .map(this::toCustomerVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), records, (long) current, (long) size);
    }

    private SalesCustomerVO toCustomerVO(SalesCustomer c) {
        SalesCustomerVO vo = new SalesCustomerVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setPhone(c.getPhone());
        vo.setEmail(c.getEmail());
        vo.setChannel(c.getChannel());
        vo.setIsNew(c.getIsNew());
        vo.setFirstPurchaseDate(c.getFirstPurchaseDate());
        vo.setRepeatPurchaseCount(c.getRepeatPurchaseCount());
        vo.setLevel(c.getLevel());
        return vo;
    }
}
