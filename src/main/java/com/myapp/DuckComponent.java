package com.myapp;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

// 显示鸭子图片

public class DuckComponent extends JComponent {
    private final String name;
    private final boolean isDonald;
    private final List<String> clothing = new ArrayList<>();
    private boolean isSelected = false; // 是否被选中（用于交互效果）
    
    // 动画处理器引用
    private DuckAnimationHandler animationHandler;
    
    // 声音处理器引用
    private DuckSoundHandler soundHandler;
        
    //图片相关属性
    private Image duckImage; // 鸭子图片
    private boolean imageLoaded = false; // 图片是否成功加载
    
    //添加小鸭子默认图片smallduck.png
    private Image smallDuckImage; // 小鸭子默认图片
    
    //情绪状态相关属性
    private String currentEmotion = "normal"; // 当前情绪：normal/happy/sad/confident
    private Image happyImage; // 开心状态图片
    private Image sadImage; // 伤心状态图片
    private Image confidentImage; // 自信状态图片
    private boolean emotionImagesLoaded = false; // 情绪图片是否加载成功
    
    // 动画相关属性 
    private Point originalPosition; // 原始位置
    private boolean isAnimating = false; // 是否正在播放动画
    private Timer animationTimer; // 动画计时器
    
    // 服装图片相关属性 
    private Image hatImage; // 帽子图片
    private Image hudiejieImage; // 蝴蝶结图片
    private Image dayiImage; // 大衣图片
    private Image maoyiImage; // 毛衣图片
    private Image beidaikuImage; // 背带裤图片
    private Image mingpaoImage; // 明袍图片
    private boolean clothingImagesLoaded = false; // 服装图片是否加载成功
    
    //便于管理不同季节的服装
    public static final String CATEGORY_WINTER = "冬装";
    public static final String CATEGORY_SUMMER = "夏装";
    public static final String CATEGORY_ACCESSORY = "装饰";
    
    //服装风格类型，但是最后应该没用到。。。
    public static final String STYLE_CASUAL = "休闲装";
    public static final String STYLE_FORMAL = "正装";
    public static final String STYLE_SPORTS = "运动装";
    
    private String currentStyle = STYLE_CASUAL; // 默认服装风格
    
    public DuckComponent(String name, boolean isDonald) {
        this.name = name;
        this.isDonald = isDonald;
        setPreferredSize(new Dimension(220, 300)); // 增大小鸭子尺寸
        
        // 记录原始位置 - 软工思想：位置管理
        // 好处：保存组件的原始位置，用于动画后恢复
        originalPosition = new Point(0, 0);
        
        // 加载鸭子图片
        //提前加载图片，避免绘制时延迟
        loadDuckImage();
        
        // 确保组件可显示
        setOpaque(false);
        
        // 设置组件无焦点框
        setFocusable(false);
        this.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (this.isShowing()) {
                    //保存原始位置
                    // 在组件显示时保存当前位置作为原始位置
                    if (originalPosition.x == 0 && originalPosition.y == 0) {
                        originalPosition = getLocation();
                    }
                    repaint();
                }
            }
        });
    }
    
    //加载鸭子图片
    private void loadDuckImage() {
        try {
            if (isDonald) {
                // 唐老鸭只加载一种图片
                String imagePath = "/images/largeduck.png";
                java.net.URL imageUrl = getClass().getResource(imagePath);
                
                if (imageUrl != null) {
                    ImageIcon imageIcon = new ImageIcon(imageUrl);
                    int maxWidth = 180;
                    int maxHeight = 220;
                    
                    int originalWidth = imageIcon.getIconWidth();
                    int originalHeight = imageIcon.getIconHeight();
                    
                    double scale = Math.min(
                        (double) maxWidth / originalWidth,
                        (double) maxHeight / originalHeight
                    );
                    
                    int newWidth = (int) (originalWidth * scale);
                    int newHeight = (int) (originalHeight * scale);
                    
                    duckImage = imageIcon.getImage().getScaledInstance(
                        newWidth, newHeight, Image.SCALE_SMOOTH
                    );
                    imageLoaded = true;
                } else {
                    System.err.println("无法找到唐老鸭图片: " + imagePath);
                }
            } else {
                // 小鸭子先加载默认图片，再加载情绪图片
                loadSmallDuckImage();
                loadEmotionImages();
                
                // 加载服装图片
                loadClothingImages();
            }
        } catch (Exception e) {
            System.err.println("加载鸭子图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            imageLoaded = false;
        }
        
        // 触发重绘
        if (imageLoaded) {
            SwingUtilities.invokeLater(this::repaint);
        }
    }
    
    //加载小鸭子默认图片
    private void loadSmallDuckImage() {
        try {
            String imagePath = "/images/smallduck.png";
            java.net.URL imageUrl = getClass().getResource(imagePath);
            
            if (imageUrl != null) {
                ImageIcon imageIcon = new ImageIcon(imageUrl);
                // 放大图片尺寸
                int maxWidth = 200;
                int maxHeight = 250;
                
                int originalWidth = imageIcon.getIconWidth();
                int originalHeight = imageIcon.getIconHeight();
                
                double scale = Math.min(
                    (double) maxWidth / originalWidth,
                    (double) maxHeight / originalHeight
                );
                
                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);
                
                smallDuckImage = imageIcon.getImage().getScaledInstance(
                    newWidth, newHeight, Image.SCALE_SMOOTH
                );
                
                // 设置默认图片为小鸭子图片
                duckImage = smallDuckImage;
                currentEmotion = "normal";
                imageLoaded = true; //置图片加载标志
                
                System.out.println("成功加载小鸭子默认图片: smallduck.png");
            } else {
                System.err.println("无法找到小鸭子默认图片: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("加载小鸭子默认图片时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    //加载情绪图片
    private void loadEmotionImages() {
        try {
            // 加载三种情绪图片
            String[] emotionNames = {"happy", "sad", "confident"};
            Image[] emotionImages = new Image[3]; // 正确初始化数组
            
            boolean allLoaded = true;
            
            for (int i = 0; i < emotionNames.length; i++) {
                // 使用正确的图片路径
                String imagePath = "/images/" + emotionNames[i] + ".png";
                java.net.URL imageUrl = getClass().getResource(imagePath);
                
                if (imageUrl != null) {
                    ImageIcon imageIcon = new ImageIcon(imageUrl);
                    // 放大图片尺寸
                    int maxWidth = 200;
                    int maxHeight = 250;
                    
                    int originalWidth = imageIcon.getIconWidth();
                    int originalHeight = imageIcon.getIconHeight();
                    
                    // 计算缩放比例
                    double scale = Math.min(
                        (double) maxWidth / originalWidth,
                        (double) maxHeight / originalHeight
                    );
                    
                    int newWidth = (int) (originalWidth * scale);
                    int newHeight = (int) (originalHeight * scale);
                    
                    emotionImages[i] = imageIcon.getImage().getScaledInstance(
                        newWidth, newHeight, Image.SCALE_SMOOTH
                    );
                } else {
                    System.err.println("无法找到小鸭子情绪图片: " + imagePath);
                    allLoaded = false;
                }
            }
            
            // 只有所有图片都加载成功才更新
            if (allLoaded) {
                // 更新情绪图片数组
                this.happyImage = emotionImages[0];
                this.sadImage = emotionImages[1];
                this.confidentImage = emotionImages[2];
                emotionImagesLoaded = true;
            } else {
                emotionImagesLoaded = false;
            }
        } catch (Exception e) {
            System.err.println("加载情绪图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            emotionImagesLoaded = false;
        }
    }
    
    //加载服装图片
    private void loadClothingImages() {
        try {
            // 加载所有服装图片
            // 冬装：大衣、毛衣
            loadClothingImage("大衣", "dayi");
            loadClothingImage("毛衣", "maoyi");
            
            // 夏装：背带裤、明袍
            loadClothingImage("背带裤", "beidaiku");
            loadClothingImage("明袍", "mingpao");
            
            // 装饰：帽子、蝴蝶结
            loadClothingImage("帽子", "hat");
            loadClothingImage("蝴蝶结", "hudiejie");
            
            clothingImagesLoaded = true;
            
        } catch (Exception e) {
            System.err.println("加载服装图片时发生错误: " + e.getMessage());
            e.printStackTrace();
            clothingImagesLoaded = false;
        }
    }
    
    //加载单个服装图片
    private void loadClothingImage(String clothingName, String imageFileName) {
        try {
            //使用中文图片文件名
            String imagePath = "/images/" + imageFileName + ".png";
            java.net.URL imageUrl = getClass().getResource(imagePath);
            
            if (imageUrl != null) {
                ImageIcon imageIcon = new ImageIcon(imageUrl);
                //放大服装图片尺寸
                int maxWidth = 120;
                int maxHeight = 140;
                
                int originalWidth = imageIcon.getIconWidth();
                int originalHeight = imageIcon.getIconHeight();
                
                // 计算缩放比例
                double scale = Math.min(
                    (double) maxWidth / originalWidth,
                    (double) maxHeight / originalHeight
                );
                
                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);
                
                Image scaledImage = imageIcon.getImage().getScaledInstance(
                    newWidth, newHeight, Image.SCALE_SMOOTH
                );
                
                //根据服装名称设置对应的图片属性
                switch (clothingName) {
                    case "帽子":
                        hatImage = scaledImage;
                        break;
                    case "蝴蝶结":
                        hudiejieImage = scaledImage;
                        break;
                    case "大衣":
                        dayiImage = scaledImage;
                        break;
                    case "毛衣":
                        maoyiImage = scaledImage;
                        break;
                    case "背带裤":
                        beidaikuImage = scaledImage;
                        break;
                    case "明袍":
                        mingpaoImage = scaledImage;
                        break;
                    default:
                        System.out.println("未知服装类型: " + clothingName);
                        break;
                }
                
                System.out.println("成功加载服装图片: " + clothingName);
            } else {
                //服装图片缺失时的处理
                System.out.println("未找到服装图片（可选）: " + imagePath + " 对应服装: " + clothingName);
            }
        } catch (Exception e) {
            System.err.println("加载服装图片失败 " + clothingName + ": " + e.getMessage());
        }
    }
    
    //执行随机情绪动画
    public void performRandomEmotionAnimation() {
        if (isDonald || !emotionImagesLoaded || isAnimating) {
            return; // 唐老鸭或图片未加载或正在动画时不执行
        }
        
        //随机选择情绪
        String[] emotions = {"happy", "sad", "confident"};
        Random random = new Random();
        String randomEmotion = emotions[random.nextInt(emotions.length)];
        
        //设置情绪
        setEmotion(randomEmotion);
        
        //播放对应声音
        if (soundHandler != null) {
            soundHandler.playCorrespondingSound(randomEmotion);
        }
        
        //使用DuckAnimationHandler播放动画
        if (animationHandler != null) {
            animationHandler.playAnimationAndReturn(this, randomEmotion, originalPosition);
        }
    }
    
    
    //设置鸭子情绪
    public void setEmotion(String emotion) {
        if (isDonald || !emotionImagesLoaded) {
            return; // 唐老鸭或图片未加载时不支持情绪切换
        }
        
        switch (emotion.toLowerCase()) {
            case "happy":
                duckImage = happyImage;
                currentEmotion = "happy";
                break;
            case "sad":
                duckImage = sadImage;
                currentEmotion = "sad";
                break;
            case "confident":
                duckImage = confidentImage;
                currentEmotion = "confident";
                break;
            case "normal":
            default:
                duckImage = happyImage; // 默认开心状态
                currentEmotion = "happy";
                break;
        }
        
        repaint(); //触发重绘
    }
    
    //获取当前情绪
    public String getCurrentEmotion() {
        return currentEmotion;
    }
    
    public String getName() {
        return name;
    }
    
    //添加服装
    public void addClothing(String item) {
        //判断服装类型
        String category = getClothingCategory(item);
        
        if (CATEGORY_WINTER.equals(category)) {
            //处理冬装（只能穿一件）
            removeClothingByCategory(CATEGORY_WINTER);
            removeClothingByCategory(CATEGORY_SUMMER);
        } else if (CATEGORY_SUMMER.equals(category)) {
            //处理夏装（只能穿一件）
            removeClothingByCategory(CATEGORY_WINTER);
            removeClothingByCategory(CATEGORY_SUMMER);
        }
        //装饰品类服装可以重复穿戴
        
        //避免重复穿戴相同物品
        clothing.removeIf(c -> c.equals(item));
        
        //添加新物品
        clothing.add(item);
        
        //立即强制重绘
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }
    
    //根据服装名称获取分类

    private String getClothingCategory(String clothingName) {
        switch (clothingName) {
            case "大衣":
            case "毛衣":
                return CATEGORY_WINTER;
            case "背带裤":
            case "明袍":
                return CATEGORY_SUMMER;
            case "帽子":
            case "蝴蝶结":
                return CATEGORY_ACCESSORY;
            default:
                return "未知";
        }
    }
    
    //移除指定分类的所有服装
    private void removeClothingByCategory(String category) {
        if (CATEGORY_WINTER.equals(category)) {
            clothing.removeIf(c -> "大衣".equals(c) || "毛衣".equals(c));
        } else if (CATEGORY_SUMMER.equals(category)) {
            clothing.removeIf(c -> "背带裤".equals(c) || "明袍".equals(c));
        }
        // 装饰品不移除
    }
    
    //获取当前穿着的衣服
    public String getCurrentClothing() {
        for (String item : clothing) {
            String category = getClothingCategory(item);
            if (CATEGORY_WINTER.equals(category) || CATEGORY_SUMMER.equals(category)) {
                return item;
            }
        }
        return null;
    }
    
    //获取当前佩戴的装饰品
    public List<String> getCurrentAccessories() {
        List<String> accessories = new ArrayList<>();
        for (String item : clothing) {
            if (CATEGORY_ACCESSORY.equals(getClothingCategory(item))) {
                accessories.add(item);
            }
        }
        return accessories;
    }
    
    public void removeClothing(String item) {
        clothing.remove(item);
        //实时重绘
        repaint();
    }
    
    public List<String> getClothing() {
        return new ArrayList<>(clothing);
    }
    
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setStyle(String style) {
        this.currentStyle = style;
        repaint();
    }
    
    public String getStyle() {
        return currentStyle;
    }
    
    //设置动画状态 - 供DuckAnimationHandler使用
    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }
    
    //设置为正常图片 - 供DuckAnimationHandler使用
    public void setToNormalImage() {
        if (smallDuckImage != null) {
            duckImage = smallDuckImage;
            currentEmotion = "normal";
        }
    }
    
    //获取原始位置 - 供DuckAnimationHandler使用
    public Point getOriginalPosition() {
        return originalPosition;
    }
    
    //设置动画处理器
    public void setAnimationHandler(DuckAnimationHandler animationHandler) {
        this.animationHandler = animationHandler;
    }
    
    //设置声音处理器
    public void setSoundHandler(DuckSoundHandler soundHandler) {
        this.soundHandler = soundHandler;
    }
        
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        
        // 如果被选中，添加一些视觉效果
        if (isSelected) {
            // 添加发光效果
            g2d.setColor(new Color(255, 255, 200, 100));
            g2d.fillOval(centerX - 80, 10, 160, 260);
        }
        
        //只使用真实图片，删除手绘代码
        if (imageLoaded && duckImage != null) {
            // 使用真实图片绘制鸭子
            int imageWidth = duckImage.getWidth(null);
            int imageHeight = duckImage.getHeight(null);
            int imageX = centerX - imageWidth / 2;
            int imageY = 50; // 图片绘制的Y位置
            
            // 绘制鸭子图片
            g2d.drawImage(duckImage, imageX, imageY, this);
        }
        // 绘制配饰
        drawAccessories(g2d, centerX, isDonald ? 60 : 70);
        
        // 绘制名字标签
        g2d.setColor(new Color(255, 255, 255, 230));
        Font font = new Font("SansSerif", Font.BOLD, 16);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        int labelX = (width - nameWidth - 20) / 2;
        int labelY = height - 35;
        
        g2d.fillRoundRect(labelX, labelY, nameWidth + 20, 30, 15, 15);
        g2d.setColor(new Color(70, 130, 180));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(labelX, labelY, nameWidth + 20, 30, 15, 15);
        
        g2d.setColor(isDonald ? new Color(0, 51, 153) : new Color(255, 140, 0));
        g2d.drawString(name, labelX + 10, labelY + 20);
        
        g2d.dispose();
    }
    
    //绘制所有服装和配饰
    private void drawAccessories(Graphics2D g2d, int centerX, int startY) {
        //按照层次顺序绘制服装
        
        // 第一层：冬装（大衣、毛衣）
        if (clothing.contains("大衣") && clothingImagesLoaded && dayiImage != null) {
            int dayiWidth = dayiImage.getWidth(null);
            int dayiHeight = dayiImage.getHeight(null);
            int dayiX = centerX - dayiWidth / 2;
            int dayiY = startY + 40; // 调整大衣位置到身体上部
            
            g2d.drawImage(dayiImage, dayiX, dayiY, null);
        }
        
      if (clothing.contains("毛衣") && clothingImagesLoaded && maoyiImage != null) {
    // 1. 获取原图尺寸
    int originalWidth = maoyiImage.getWidth(null);
    int originalHeight = maoyiImage.getHeight(null);

    //
    double scale = 2.0; 
    int newWidth = (int) (originalWidth * scale);   
    int newHeight = (int) (originalHeight * scale);

    // 3. 计算坐标
    int maoyiX = centerX - newWidth / 2;

    // Y轴计算
    int originalY = startY + 45; 
    int centerY = originalY + (originalHeight / 2);
    
    //再往下挪一点
    int offsetDown = 15; 
    
    // 最终位置 = (居中位置) + (往下挪的距离)
    int maoyiY = centerY - (newHeight / 2) + offsetDown;         

    // 4. 绘制
    g2d.drawImage(maoyiImage, maoyiX, maoyiY, newWidth, newHeight, null);
}
        // 第二层：夏装（背带裤、明袍）
        if (clothing.contains("背带裤") && clothingImagesLoaded && beidaikuImage != null) {
            int beidaikuWidth = beidaikuImage.getWidth(null);
            int beidaikuHeight = beidaikuImage.getHeight(null);
            int beidaikuX = centerX - beidaikuWidth / 2;
            int beidaikuY = startY + 50; // 调整背带裤位置到身体下部
            
            g2d.drawImage(beidaikuImage, beidaikuX, beidaikuY, null);
        }
        
        if (clothing.contains("明袍") && clothingImagesLoaded && mingpaoImage != null) {
            int mingpaoWidth = mingpaoImage.getWidth(null);
            int mingpaoHeight = mingpaoImage.getHeight(null);
            int mingpaoX = centerX - mingpaoWidth / 2;
            int mingpaoY = startY + 65; // 调整明袍位置到身体上部
            
            g2d.drawImage(mingpaoImage, mingpaoX, mingpaoY, null);
        }
        
        // 第三层：装饰品（帽子、蝴蝶结）
        if (clothing.contains("帽子") && clothingImagesLoaded && hatImage != null) {
            int hatWidth = hatImage.getWidth(null);
            int hatHeight = hatImage.getHeight(null);
            int hatX = centerX - hatWidth / 2;
            int hatY = startY - 40; // 调整帽子位置到头顶
            
            g2d.drawImage(hatImage, hatX, hatY, null);
        }
        
        if (clothing.contains("蝴蝶结") && clothingImagesLoaded && hudiejieImage != null) {
            int bowWidth = hudiejieImage.getWidth(null);
            int bowHeight = hudiejieImage.getHeight(null);
            int bowX = centerX - bowWidth / 2;
            int bowY = startY + 30; // 调整蝴蝶结位置到颈部
            
            g2d.drawImage(hudiejieImage, bowX, bowY, null);
        }
    }
}
