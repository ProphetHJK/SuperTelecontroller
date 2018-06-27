package cn.edu.zjut.jinkai.supertelecontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.URL;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;

/**
 * 取得linux系统下的cpu、内存信息
 *
 *
 */
final class LinuxSystemTool
{
    /**
     * get memory by used info
     *
     * @return int[] result
     * result.length==4;int[0]=MemTotal;int[1]=MemFree;int[2]=SwapTotal;int[3]=SwapFree;
     * @throws IOException
     * @throws InterruptedException
     */
    public static int[] getMemInfo() throws IOException, InterruptedException
    {
        File file = new File("/proc/meminfo");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        int[] result = new int[4];
        String str = null;
        StringTokenizer token = null;
        while((str = br.readLine()) != null)
        {
            token = new StringTokenizer(str);
            if(!token.hasMoreTokens())
                continue;

            str = token.nextToken();
            if(!token.hasMoreTokens())
                continue;

            if(str.equalsIgnoreCase("MemTotal:"))
                result[0] = Integer.parseInt(token.nextToken());
            else if(str.equalsIgnoreCase("MemFree:"))
                result[1] = Integer.parseInt(token.nextToken());
            else if(str.equalsIgnoreCase("SwapTotal:"))
                result[2] = Integer.parseInt(token.nextToken());
            else if(str.equalsIgnoreCase("SwapFree:"))
                result[3] = Integer.parseInt(token.nextToken());
        }

        return result;
    }

    /**
     * get memory by used info
     *
     * @return float efficiency
     * @throws IOException
     * @throws InterruptedException
     */
    public static float getCpuInfo() throws IOException, InterruptedException
    {
        File file = new File("/proc/stat");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        StringTokenizer token = new StringTokenizer(br.readLine());
        token.nextToken();
        int user1 = Integer.parseInt(token.nextToken());
        int nice1 = Integer.parseInt(token.nextToken());
        int sys1 = Integer.parseInt(token.nextToken());
        int idle1 = Integer.parseInt(token.nextToken());

        Thread.sleep(1000);

        br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)));
        token = new StringTokenizer(br.readLine());
        token.nextToken();
        int user2 = Integer.parseInt(token.nextToken());
        int nice2 = Integer.parseInt(token.nextToken());
        int sys2 = Integer.parseInt(token.nextToken());
        int idle2 = Integer.parseInt(token.nextToken());

        return (float)((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float)((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
    }
}
class HServerApp implements Runnable {
    public int port;

    public HServerApp(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                //等待client的请求
                System.out.println("waiting...");
                Socket socket = server.accept();
                // 接收客户端的数据
                //while(socket!=null){
                while(socket!=null){

                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    String string = in.readUTF();
                    System.out.println("client:" + string);
                    Thread.sleep(50);

//}
                    // 发送给客户端数据
                    int[] memInfo = LinuxSystemTool.getMemInfo();
                    //System.out.println("MemTotal：" + memInfo[0]);
                    System.out.println("MemFree：" + memInfo[1]);
                    //System.out.println("SwapTotal：" + memInfo[2]);
                    //System.out.println("SwapFree：" + memInfo[3]);
                    //System.out.println("CPU利用率：" + LinuxSystemTool.getCpuInfo());
                    //while(true)
                    //{
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(LinuxSystemTool.getMemInfo()[1]+"");
                    out.writeUTF(LinuxSystemTool.getCpuInfo()+"");
                    out.writeUTF(LinuxSystemTool.getMemInfo()[3]+"");
                    out.writeUTF(LinuxSystemTool.getMemInfo()[0]+"");

                    //out.close();
                    //Thread.sleep(5000);

                }
            }
            //socket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        HServerApp serverApp = new HServerApp(9050);
        serverApp.run();
    }
}
