package ju.pioneer.cloud_disk.utils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * 验证码图片生成工具
 */
public class CaptchaUtils {

    // 验证码字符集（去掉易混淆的 0 O I l）
    private static final String CHAR_ARRAY = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int WIDTH = 100;    // 图片宽度
    private static final int HEIGHT = 40;   // 图片高度
    private static final int LENGTH = 4;    // 验证码长度
    private static final Random RANDOM = new Random();

    /**
     * 生成验证码图片 + 验证码文本
     * @return [0] 验证码文本, [1] 图片对象
     */
    public static Object[] createCaptcha() {
        // 1. 创建图片对象
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 2. 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 3. 生成随机验证码
        StringBuilder captchaText = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            char c = CHAR_ARRAY.charAt(RANDOM.nextInt(CHAR_ARRAY.length()));
            captchaText.append(c);

            // 4. 设置字符颜色、字体
            g.setColor(getRandomColor());
            g.setFont(new Font("Arial", Font.BOLD, 28));

            // 5. 字符位置 + 轻微倾斜
            int x = i * WIDTH / (LENGTH + 1) + 5;
            int y = HEIGHT / 2 + 10;
            g.drawString(String.valueOf(c), x, y);
        }

        // 6. 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandomColor());
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 7. 添加干扰点
        for (int i = 0; i < 30; i++) {
            g.setColor(getRandomColor());
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.drawOval(x, y, 1, 1);
        }

        g.dispose();
        return new Object[]{captchaText.toString(), image};
    }

    /**
     * 生成随机颜色
     */
    private static Color getRandomColor() {
        int r = RANDOM.nextInt(150);
        int g = RANDOM.nextInt(150);
        int b = RANDOM.nextInt(150);
        return new Color(r, g, b);
    }

    // ==================== 测试：生成图片到本地 ====================
    public static void main(String[] args) throws IOException {
        Object[] captcha = createCaptcha();
        String code = (String) captcha[0];
        BufferedImage image = (BufferedImage) captcha[1];

        System.out.println("验证码：" + code);
        // 保存到桌面
        ImageIO.write(image, "png", new File("C:\\Users\\86199\\Desktop\\captcha.png"));
    }
}