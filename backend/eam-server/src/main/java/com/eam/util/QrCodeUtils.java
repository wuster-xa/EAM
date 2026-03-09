package com.eam.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 * 
 * 功能说明：
 * 使用 Google ZXing 库生成二维码图片
 * 将资产 UUID 编码为二维码，支持移动端扫码盘点
 * 
 * 技术要点：
 * - 二维码格式：QR_CODE
 * - 输出格式：Base64 字符串（可直接在前端显示）
 * - 支持自定义尺寸
 * 
 * 应用场景：
 * 1. 资产入库时自动生成二维码
 * 2. 资产详情页展示二维码
 * 3. 移动端扫码盘点
 * 
 * @author 毕业设计项目组
 */
@Slf4j
public class QrCodeUtils {

    /**
     * 默认二维码宽度（像素）
     */
    private static final int DEFAULT_WIDTH = 200;

    /**
     * 默认二维码高度（像素）
     */
    private static final int DEFAULT_HEIGHT = 200;

    /**
     * 生成二维码 Base64 字符串
     * 
     * 流程：
     * 1. 创建 QRCodeWriter 编码器
     * 2. 设置编码参数（字符集、边距等）
     * 3. 生成二维码矩阵
     * 4. 将矩阵转换为图片
     * 5. 将图片转换为 Base64 字符串
     * 
     * @param content 二维码内容（通常是资产 UUID）
     * @return Base64 编码的二维码图片字符串
     */
    public static String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码 Base64 字符串（自定义尺寸）
     * 
     * @param content 二维码内容
     * @param width 二维码宽度（像素）
     * @param height 二维码高度（像素）
     * @return Base64 编码的二维码图片字符串
     */
    public static String generateQrCodeBase64(String content, int width, int height) {
        try {
            // 1. 设置编码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            // 设置字符编码
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            // 设置边距（单位：模块数）
            hints.put(EncodeHintType.MARGIN, 1);
            // 设置纠错等级（L-7%, M-15%, Q-25%, H-30%）
            // H 级纠错能力最强，即使二维码部分损坏也能识别
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);

            // 2. 创建 QRCodeWriter 编码器
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // 3. 生成二维码矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 4. 将矩阵转换为图片字节数组
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            // 5. 转换为 Base64 字符串
            // 添加 data:image/png;base64, 前缀，可直接在 HTML img 标签中使用
            String base64String = Base64.getEncoder().encodeToString(qrCodeBytes);
            return "data:image/png;base64," + base64String;

        } catch (Exception e) {
            log.error("生成二维码失败: content={}, error={}", content, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 生成二维码字节数组
     * 
     * 用于需要直接保存二维码图片文件的场景
     * 
     * @param content 二维码内容
     * @return 二维码图片字节数组
     */
    public static byte[] generateQrCodeBytes(String content) {
        return generateQrCodeBytes(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码字节数组（自定义尺寸）
     * 
     * @param content 二维码内容
     * @param width 二维码宽度
     * @param height 二维码高度
     * @return 二维码图片字节数组
     */
    public static byte[] generateQrCodeBytes(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("生成二维码失败: content={}, error={}", content, e.getMessage(), e);
            return null;
        }
    }
}
