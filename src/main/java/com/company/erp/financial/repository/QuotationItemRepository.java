package com.company.erp.financial.repository;

import com.company.erp.financial.entity.QuotationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface QuotationItemRepository extends JpaRepository<QuotationItem, Long> {

    // Find by quotation
    List<QuotationItem> findByQuotationIdAndActiveTrue(Long quotationId);

    @Query("SELECT qi FROM QuotationItem qi WHERE qi.quotation.id = :quotationId AND qi.active = true ORDER BY qi.itemOrder ASC")
    List<QuotationItem> findByQuotationIdOrderByItemOrder(@Param("quotationId") Long quotationId);

    // Calculate totals
    @Query("SELECT SUM(qi.amount) FROM QuotationItem qi WHERE qi.quotation.id = :quotationId AND qi.active = true")
    BigDecimal calculateTotalByQuotationId(@Param("quotationId") Long quotationId);

    @Query("SELECT COUNT(qi) FROM QuotationItem qi WHERE qi.quotation.id = :quotationId AND qi.active = true")
    long countByQuotationId(@Param("quotationId") Long quotationId);

    // Find by category and account head
    List<QuotationItem> findByCategoryAndActiveTrue(String category);

    List<QuotationItem> findByAccountHeadAndActiveTrue(String accountHead);

    @Query("SELECT qi.category, COUNT(qi), SUM(qi.amount) FROM QuotationItem qi WHERE qi.active = true AND qi.category IS NOT NULL GROUP BY qi.category")
    List<Object[]> getItemStatsByCategory();

    @Query("SELECT qi.accountHead, COUNT(qi), SUM(qi.amount) FROM QuotationItem qi WHERE qi.active = true AND qi.accountHead IS NOT NULL GROUP BY qi.accountHead")
    List<Object[]> getItemStatsByAccountHead();

    // Delete methods
    @Modifying
    @Query("UPDATE QuotationItem qi SET qi.active = false WHERE qi.quotation.id = :quotationId")
    void deactivateByQuotationId(@Param("quotationId") Long quotationId);

    @Modifying
    void deleteByQuotationId(Long quotationId);

    // Vendor-related queries
    List<QuotationItem> findByVendorNameContainingIgnoreCaseAndActiveTrue(String vendorName);

    @Query("SELECT qi.vendorName, COUNT(qi), SUM(qi.amount) FROM QuotationItem qi WHERE qi.active = true AND qi.vendorName IS NOT NULL GROUP BY qi.vendorName ORDER BY SUM(qi.amount) DESC")
    List<Object[]> getTopVendorsByAmount();
}