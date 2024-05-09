import cn.hutool.crypto.SmUtil;
import com.dtflys.forest.Forest;

public class Test {

    public static void main(String[] args) {
        String s = SmUtil.sm3WithSalt("janime@salt".getBytes()).digestHex("janime@pwd");
        System.out.println(s.toUpperCase());

        String html = Forest.get("https://www.live1024.cn").executeAsString();
        System.out.println("html: >>>>> ");
        System.out.println(html);

        String img = "https://ftp.bmp.ovh/imgs/2022/02/b5cfa00f98da6b6f.jpg";
        Forest.get(img).async().setDownloadFile("D:\\test\\files1", "b5cfa00f98da6b6f.jpg").execute();

        System.out.println(">>>>> test end.");
    }

}
