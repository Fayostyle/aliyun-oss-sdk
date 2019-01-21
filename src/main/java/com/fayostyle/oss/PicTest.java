package com.fayostyle.oss;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author keith.huang
 * @date 2019/1/15 23:33
 */
public class PicTest {

    public static void main(String[] args) {
        try
        {
            File file = new File("/mount_pic/3dfile/d/flash/2019/01/15/10/20190115101365606541547520714014.jpg");
            if (!file.exists())
            {
                if (!file.getParentFile().exists())
                {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            try (FileOutputStream fos = new FileOutputStream(file))
            {
                System.out.println("写入");
            }
        }
        catch (Exception e)
        {

            System.out.println("出错");
            e.printStackTrace();
        }
    }
}
