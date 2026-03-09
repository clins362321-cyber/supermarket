package org.example.supermarket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.supermarket.dao.ProcurementApplyItemMapper;
import org.example.supermarket.dao.ProcurementApplyMapper;
import org.example.supermarket.dao.ProductMapper;
import org.example.supermarket.dao.SupplierMapper;
import org.example.supermarket.dto.ProcurementApplyCreateDto;
import org.example.supermarket.dto.ProcurementApplyItemDto;
import org.example.supermarket.dto.WarehouseInitiateProcurementDto;
import org.example.supermarket.entity.ProcurementApply;
import org.example.supermarket.entity.ProcurementApplyItem;
import org.example.supermarket.entity.Product;
import org.example.supermarket.entity.Supplier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ProcurementService {

    private final SupplierMapper supplierMapper;
    private final ProcurementApplyMapper applyMapper;
    private final ProcurementApplyItemMapper itemMapper;
    private final ProductMapper productMapper;

    public ProcurementService(SupplierMapper supplierMapper, ProcurementApplyMapper applyMapper,
                              ProcurementApplyItemMapper itemMapper, ProductMapper productMapper) {
        this.supplierMapper = supplierMapper;
        this.applyMapper = applyMapper;
        this.itemMapper = itemMapper;
        this.productMapper = productMapper;
    }

    public List<Supplier> listSuppliers() {
        return supplierMapper.selectList(null);
    }

    public Supplier createSupplier(Supplier s) {
        s.setId(null);
        s.setStatus(s.getStatus() != null ? s.getStatus() : "ACTIVE");
        supplierMapper.insert(s);
        return s;
    }

    public Supplier updateSupplier(Supplier s) {
        if (s.getId() == null) return null;
        supplierMapper.updateById(s);
        return supplierMapper.selectById(s.getId());
    }

    public List<ProcurementApply> listApplies(String status, String username) {
        QueryWrapper<ProcurementApply> q = new QueryWrapper<ProcurementApply>().orderByDesc("created_at", "id");
        if (status != null && !status.isBlank()) {
            q.eq("status", status.trim());
        }
        if (username != null && !username.isBlank()) {
            q.eq("username", username.trim());
        }
        return applyMapper.selectList(q);
    }

    public List<ProcurementApply> listPendingApprovals() {
        return applyMapper.selectList(
            new QueryWrapper<ProcurementApply>().eq("status", "PENDING").orderByAsc("created_at"));
    }

    public ProcurementApply createApply(ProcurementApply apply) {
        apply.setId(null);
        apply.setApplyNo("PA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        apply.setStatus(apply.getStatus() != null ? apply.getStatus() : "PENDING");
        apply.setCreatedAt(LocalDateTime.now());
        applyMapper.insert(apply);
        return apply;
    }

    public ProcurementApply createApplyWithItems(ProcurementApplyCreateDto dto) {
        ProcurementApply apply = new ProcurementApply();
        apply.setTitle(dto.getTitle() != null ? dto.getTitle() : "采购申请");
        apply.setSupplierId(dto.getSupplierId());
        apply.setUsername(dto.getUsername());
        apply.setStatus("PENDING");
        apply.setCreatedAt(LocalDateTime.now());
        apply.setApplyNo("PA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        applyMapper.insert(apply);

        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (ProcurementApplyItemDto it : dto.getItems()) {
                if (it.getQty() == null || it.getQty() <= 0) continue;
                ProcurementApplyItem item = new ProcurementApplyItem();
                item.setApplyId(apply.getId());
                item.setProductId(it.getProductId());
                item.setProductName(it.getProductName() != null ? it.getProductName() : "");
                item.setQty(it.getQty());
                item.setPrice(it.getPrice());
                itemMapper.insert(item);
            }
        }
        return apply;
    }

    public ProcurementApply createApplyFromWarehouse(WarehouseInitiateProcurementDto dto) {
        if (dto.getProductId() == null || dto.getQty() == null || dto.getQty() <= 0) {
            throw new IllegalArgumentException("商品和数量必填");
        }
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null) throw new IllegalArgumentException("商品不存在");

        ProcurementApplyCreateDto create = new ProcurementApplyCreateDto();
        create.setTitle("库存预警采购-" + p.getName());
        create.setSupplierId(dto.getSupplierId());
        create.setUsername(dto.getUsername());

        ProcurementApplyItemDto item = new ProcurementApplyItemDto();
        item.setProductId(p.getId());
        item.setProductName(p.getName());
        item.setQty(dto.getQty());
        item.setPrice(p.getPrice());
        create.setItems(List.of(item));

        return createApplyWithItems(create);
    }

    public ProcurementApply approve(Long id, String approverUsername) {
        ProcurementApply apply = applyMapper.selectById(id);
        if (apply == null) throw new IllegalArgumentException("申请不存在");
        if (!"PENDING".equals(apply.getStatus())) throw new IllegalArgumentException("该申请已处理");
        apply.setStatus("APPROVED");
        apply.setApproverUsername(approverUsername);
        applyMapper.updateById(apply);
        return apply;
    }

    public ProcurementApply reject(Long id, String approverUsername) {
        ProcurementApply apply = applyMapper.selectById(id);
        if (apply == null) throw new IllegalArgumentException("申请不存在");
        if (!"PENDING".equals(apply.getStatus())) throw new IllegalArgumentException("该申请已处理");
        apply.setStatus("REJECTED");
        apply.setApproverUsername(approverUsername);
        applyMapper.updateById(apply);
        return apply;
    }

    public List<ProcurementApplyItem> listApplyItems(Long applyId) {
        return itemMapper.selectList(new QueryWrapper<ProcurementApplyItem>().eq("apply_id", applyId));
    }
}
