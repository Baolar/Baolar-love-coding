package cn.edu.wust.scenicspotsystem;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

class Place{
    String name;
    String info;
    int x;
    int y;

    @Override
    public String toString(){
        return "name:"+name+"\ninfo:"+info+"\nx:"+x+"\ny:"+y+"\n";
    }
}

public class MainActivity extends AppCompatActivity {
   final  String InitPlaces = "id:0\n" +
            "name:户部巷\n" +
            "info:专坑外地人\n" +
            "x:25\n" +
            "y:50\n" +
            "id:1\n" +
            "name:黄鹤楼\n" +
            "info:黄鹤楼有电梯\n" +
            "x:8\n" +
            "y:100\n" +
            "id:2\n" +
            "name:光谷广场\n" +
            "info:偏远郊区有啥好玩的\n" +
            "x:70\n" +
            "y:130\n" +
            "id:3\n" +
            "name:东湖\n" +
            "info:风景优美有点臭\n" +
            "x:130\n" +
            "y:110\n" +
            "id:4\n" +
            "name:木兰天池\n" +
            "info:还阔以\n" +
            "x:140\n" +
            "y:50\n" +
            "id:5\n" +
            "name:欢乐谷\n" +
            "info:超级好玩\n" +
            "x:80\n" +
            "y:16\n" +
            "id:6\n" +
            "name:武汉长江大桥\n" +
            "info:万里长江第一桥\n" +
            "x:75\n" +
            "y:75\n";
    final String Inittfmatrix =
            "0,0,1,0,1,1,0\n" +
            "0,0,1,0,0,0,1\n" +
            "1,1,0,1,0,0,0\n" +
            "0,0,1,0,1,0,1\n" +
            "1,0,0,1,0,1,0\n" +
            "1,0,0,0,1,0,1\n" +
            "0,1,0,1,0,1,0\n";
    //此处用StringBuffer更好 时间仓促就不改了
    static String outputstring = new String();
    static String tfmatrix = new String();
    static HashMap<Integer, Place> placeHashMap = new HashMap();
    static Boolean IsRead = false;

    static int e = 0; //景点数
    static int n = 0; // 边的数量
    String vexName = getSDPath() + "/SpotSystem/vex.txt"; //以name存在目录中 领接矩阵
    String tfName = getSDPath() + "/SpotSystem/m01.txt"; // 01矩阵
    String dataName = getSDPath() + "/SpotSystem/data.txt";

    private ImageView iv_canvas;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private Switch mSwitch;


    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.ic_launcher_background));

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        paint = new Paint();
        iv_canvas = (ImageView) findViewById(R.id.iv_canvas);
        baseBitmap = Bitmap.createBitmap(300, 150, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(baseBitmap);
        canvas.drawColor(Color.WHITE);
        iv_canvas.setImageBitmap(baseBitmap);
        paint.setColor(Color.GRAY);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("请添加景点数据",150,75,paint);

        mSwitch = (Switch) findViewById(R.id.switch1);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    writeTxtToFile(Inittfmatrix,getSDPath()+"/SpotSystem/", "m01.txt");
                    writeTxtToFile(InitPlaces, getSDPath()+"/SpotSystem/", "vex.txt");
                    IsRead = true;
                    ReadEdge();
                    Paitting();
                    Toast.makeText(MainActivity.this, "已生成演示数据！\n" + getSDPath() + "/SpotSystem/", Toast.LENGTH_SHORT).show();
                }else {
                    writeTxtToFile("",getSDPath()+"/SpotSystem/", "m01.txt");
                    writeTxtToFile("", getSDPath()+"/SpotSystem/", "vex.txt");
                    IsRead = false;
                    placeHashMap = new HashMap<>();
                    outputstring = "";
                    tfmatrix = "";
                    TextView output = (TextView)findViewById(R.id.output);
                    output.setText("");
                    canvas.drawColor(Color.WHITE);
                    paint.setColor(Color.GRAY);
                    paint.setTextSize(30);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("请添加景点数据",150,75,paint);
                    Toast.makeText(MainActivity.this, "已清空演示数据！\n" + getSDPath() + "/SpotSystem/", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void Paitting(){
        TextView view = (TextView)findViewById(R.id.output);
        view.setText("");
        ReadEdge();
        canvas.drawColor(Color.WHITE);
        iv_canvas.setImageBitmap(baseBitmap);

        for(int i = 0;i < placeHashMap.size();i++) {
            paint.setStrokeWidth(3);
            paint.setColor(Color.RED);
            canvas.drawPoint(placeHashMap.get(i).x,placeHashMap.get(i).y,paint);

            paint.setTextSize(10);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.BLACK);
            canvas.drawText(placeHashMap.get(i).name + "(" + i + ")",placeHashMap.get(i).x,placeHashMap.get(i).y,paint);
        }

        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);

        String[]s=outputstring.split("\n");

        for(int i = 0;i < s.length;i++) {
            String[]temp=s[i].split(",");
            for(int j = 0;j < temp.length;j++){
                if(!temp[j].equals("I")&&!temp[j].equals("0")){
                    canvas.drawLine(placeHashMap.get(i).x,placeHashMap.get(i).y,placeHashMap.get(j).x,placeHashMap.get(j).y,paint);
                    paint.setTextSize(10);
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setColor(Color.GRAY);
                    canvas.drawText(temp[j],(placeHashMap.get(i).x+placeHashMap.get(j).x)/2,(placeHashMap.get(i).y+placeHashMap.get(j).y)/2,paint);
                }
            }
        }
    }

    public void ClickDelete(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText input = findViewById(R.id.editText);

        try {
            Integer keytemp = Integer.parseInt(input.getText().toString());

            if(keytemp>e||keytemp<0)
                throw new NumberFormatException();

            ArrayList<String> al = new ArrayList<>();
            String[] strs = tfmatrix.split("\n");

            for (int i = 0; i < strs.length; i++)
                if (i != keytemp)
                    al.add(strs[i]);

            tfmatrix = "";

            for (String s : al) {
                String[] strs_cow = s.split(",");
                for (int i = 0; i < strs_cow.length; i++) {
                    if (i != keytemp) {
                        tfmatrix = tfmatrix + strs_cow[i];
                        if (!(keytemp == strs_cow.length - 1 && i == strs_cow.length - 2 || i == strs_cow.length - 1))
                            tfmatrix = tfmatrix + ",";
                    }
                }

                tfmatrix = tfmatrix + "\n";
            }

            HashMap<Integer, Place> placetemp = new HashMap<>();

            for (int i = 0; i < placeHashMap.size(); i++) {
                if (i != keytemp) {
                    if (i > keytemp)
                        placetemp.put(i - 1, placeHashMap.get(i));
                    else
                        placetemp.put(i, placeHashMap.get(i));
                }
            }

            placeHashMap.clear();
            placeHashMap = placetemp;

            WriteVex();
            WritetfMatrix();
            ReadEdge();
            Paitting();
            Toast.makeText(MainActivity.this, "删除成功!", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(MainActivity.this, "请输入正确的景点编号!", Toast.LENGTH_SHORT).show();
        }
    }


    public void ClickRead(View v){
        ReadEdge();
        if(placeHashMap.size() > 0) {
            IsRead = true;
            Paitting();
            Toast.makeText(MainActivity.this, "读取成功!\n" + getSDPath() + "/SpotSystem/", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this, "读取失败!请开启演示模式或将数据存入\n" + getSDPath() + "/SpotSystem/", Toast.LENGTH_SHORT).show();
            TextView output = (TextView)findViewById(R.id.output);
            output.setText("");
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.GRAY);
            paint.setTextSize(30);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("请添加景点数据",150,75,paint);
        }
    }

    public void ClickSearch(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }

        ReadEdge();
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.output, null);
        TextView output = (TextView) view.findViewById(R.id.Output);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("查询结果");
        builder.setView(view);
        EditText input = findViewById(R.id.editText);

        try {
            Integer key = Integer.parseInt(input.getText().toString());

            if (key > e || key < 0)
                throw new NumberFormatException();

            StringBuffer others = new StringBuffer("\n相邻景点:\n");
            String[] places = outputstring.split("\n");
            String[] lin = places[key].split(",");

            for (int i = 0; i < lin.length; i++) {
                if (!lin[i].equals("0") && !lin[i].equals("I")) {
                    others.append("距离 " + i + " " + placeHashMap.get(i).name + " " + lin[i] + "\n");
                }
            }

            Place temp = placeHashMap.get(key);
            output.setText("编号:\t" + key + "\n名称:" + temp.name + "\n信息:\t" + temp.info + "\n位置:\t" + temp.x + "," + temp.y + "\n" + others);

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });

            builder.show();
        }catch(Exception e){
            Toast.makeText(MainActivity.this, "请输入正确的景点编号!", Toast.LENGTH_SHORT).show();
        }
    }

    public void ClickDFS(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }
        ReadEdge();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("导航方案");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.output, null);
        builder.setView(view);

        try {
            EditText input = findViewById(R.id.editText);
            String com = input.getText().toString().trim();
            int st = Integer.parseInt(com);

            if (st > this.e || st < 0)
                throw new NumberFormatException();

            com = TravelAll(outputstring, this.e, this.n, Integer.parseInt(com));
            com = com.replace(" ", "");

            String ans = new String();
            String[] ways = com.split("\n");

            for (int i = 0; i < ways.length; i++) {
                String s = ways[i];
                ans = ans + "方案 " + (i + 1) + " :\n";
                String[] temp = s.split("[*]");
                for (int j = 0; j < temp.length; j++)
                    ans = ans + placeHashMap.get(Integer.parseInt(temp[j])).name + " ";
                ans = ans + "\n";
            }

            TextView output = (TextView) view.findViewById(R.id.Output);

            if (ans.length() > 800)
                output.setTextSize(10);
            else
                output.setTextSize(12);
            output.setText(ans);


            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });

            builder.show();
        }catch(NumberFormatException e){
            Toast.makeText(MainActivity.this, "请输入正确的景点编号!", Toast.LENGTH_SHORT).show();
        }
        catch(Exception e){
            Toast.makeText(MainActivity.this, "无符合要求路线", Toast.LENGTH_SHORT).show();
        }
    }

    public void ClickMS(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if(placeHashMap.size() < 2){
            return;
        }

        ReadEdge();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("电网布置方案");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.output, null);
        builder.setView(view);

        System.out.println(outputstring);
        String ans = MSTree(outputstring,this.e,this.n);
        String output = "\n";
        String[] eacharc = ans.split("\n");
        int totdist = 0;

        for(String s:eacharc){
            String[]x = s.split("[*]");
            Integer temp1 = Integer.parseInt(x[0]);
            Integer temp2 = Integer.parseInt(x[1]);
            output  = output +  placeHashMap.get(temp1).name+"("+temp1 + ")<---->"+placeHashMap.get(temp2).name+"("+temp2+")\n距离: "+Integer.parseInt(x[2])+"\n\n";
            totdist = totdist + Integer.parseInt(x[2]);
            paint.setColor(Color.YELLOW);
            canvas.drawLine(placeHashMap.get(temp1).x,placeHashMap.get(temp1).y,placeHashMap.get(temp2).x,placeHashMap.get(temp2).y,paint);
        }

        TextView t = (TextView)view.findViewById(R.id.Output);
        t.setText(output);
        TextView p = (TextView)findViewById(R.id.output);
        p.setText("总长度:\n" + totdist);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "已在图中标明电网布置的最优规划", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在

        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }

        return sdDir.toString();
    }

    public String getFileContent(File file) {
        String content = "";

        if (!file.isDirectory()) {  //检查此路径名的文件是否是一个目录(文件夹)
            if (file.getName().endsWith("txt")) {//文件格式为""文件
                try {
                    InputStream instream = new FileInputStream(file);
                    if (instream != null) {
                        InputStreamReader inputreader = new InputStreamReader(instream, "UTF-8");
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";
                        //分行读取
                        while ((line = buffreader.readLine()) != null) {
                            content += line + "\n";
                        }
                        instream.close();//关闭输入流
                    }
                } catch (java.io.FileNotFoundException e) {
                    return "TestFile  The File doesn't not exist.\n"+ file.getPath();
                } catch (IOException e) {
                    return "TestFile" + e.getMessage();
                }
            }
        }

        content = content.replaceAll(" ",""); //去除全部空格
        content = content.replaceAll("\t",""); //去除制表符

        return content;
    }

    public void ReadEdge(){
        ReadPlaces();
        ReadtfMatrix();
        File file = new File(tfName);
        tfmatrix = getFileContent(file);

        this.n = 0;
        this.e = 0;

        for(int i=0;i<tfmatrix.length();i++){
            if(tfmatrix.charAt(i) == '\n')
                this.e++;
            if(tfmatrix.charAt(i)=='1')
                this.n++;
        }

        outputstring = "";
        String[]rows = tfmatrix.split("\n");

        for(int i=0;i<rows.length;i++){
            String[]cows = rows[i].split(",");
            for(int j=0;j<cows.length;j++)
                if(cows[j].equals("1")){
                    int x1=placeHashMap.get(i).x,y1=placeHashMap.get(i).y,x2=placeHashMap.get(j).x,y2=placeHashMap.get(j).y;
                    int dist = (int) Math.sqrt( (x1-x2)*(x1-x2)+  (y1-y2)*(y1-y2)   );
                    outputstring = outputstring + dist + (j==cows.length-1?"\n":",");
                }
                else if(i==j){
                    outputstring = outputstring + "0" + (j==cows.length-1?"\n":",");
                }
                else{
                    outputstring = outputstring + "I" + (j==cows.length-1?"\n":",");
                }
        }
    }

    public void ReadtfMatrix(){
        File file = new File(tfName);
        tfmatrix = getFileContent(file);
    }

    public void ReadPlaces(){
        File file = new File(vexName);
        String vex = getFileContent(file);
        placeHashMap = new HashMap<>();
        vex.replace(" ","");
        vex.replace("\t","");
        String[] temp = vex.split("id:");

        for(int i = 1;i<temp.length;i++) {
            String tempsin = temp[i];
            Place tempplace = new Place();
            String[]templine = tempsin.split("\n");
            int keytemp = Integer.parseInt(templine[0]);


            int p = templine[1].indexOf(':');
            tempplace.name = templine[1].substring(p+1);

            p = templine[2].indexOf(':');
            tempplace.info = templine[2].substring(p+1);

            p = templine[3].indexOf(':');
            tempplace.x = Integer.parseInt(templine[3].substring(p+1));

            p = templine[4].indexOf(':');
            tempplace.y = Integer.parseInt(templine[4].substring(p+1));

            placeHashMap.put(keytemp,tempplace);
        }
    }


    private void WriteVex() {
        String filePath = getSDPath()+"/SpotSystem/";
        String fileName = "vex.txt";
        String temp = new String();

        for(int i = 0;i < placeHashMap.size();i++){
            temp = temp + "id:" + i +"\n" + placeHashMap.get(i).toString();
        }
        writeTxtToFile(temp, filePath, fileName);
        ReadPlaces();
    }

    public void WritetfMatrix(){
        String filePath = getSDPath()+"/SpotSystem/";
        String fileName = "m01.txt";
        writeTxtToFile(tfmatrix, filePath, fileName);
        ReadtfMatrix();
    }

    private void writeTxtToFile(String strContent, String filePath, String fileName) {
        makeFilePath(filePath, fileName);
        String strFilePath = filePath + fileName;

        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }else{
                file.delete();
                file = new File(strFilePath);
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);

        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private static void makeRootDirectory(String filePath) {
        File file;

        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }

    public void ClickReEdit(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        final EditText name = (EditText)view.findViewById(R.id.name);
        final EditText info = (EditText)view.findViewById(R.id.info);
        final EditText x = (EditText)view.findViewById(R.id.x);
        final EditText y = (EditText)view.findViewById(R.id.y);
        final EditText id = (EditText)findViewById(R.id.editText);

        try {
            final Integer numid = Integer.parseInt(id.getText().toString().trim());
            if(numid<0||numid>e)
                throw new NumberFormatException();
            name.setText(placeHashMap.get(numid).name);
            info.setText(placeHashMap.get(numid).info);
            x.setText(placeHashMap.get(numid).x + "");
            y.setText(placeHashMap.get(numid).y + "");
            builder.setTitle("修改景点 " + numid);
            builder.setView(view);

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Place temp = new Place();
                    temp.name = name.getText().toString().trim();
                    temp.name = temp.name.replaceAll(":","");
                    temp.name = temp.name.replaceAll("id","");
                    temp.name = temp.name.replaceAll("\n","");
                    temp.info = info.getText().toString().trim();
                    temp.info = temp.info.replaceAll(":","");
                    temp.info = temp.info.replaceAll("id","");
                    temp.info = temp.info.replaceAll("\n","");
                    temp.x = Integer.parseInt(x.getText().toString().trim());
                    temp.y = Integer.parseInt(y.getText().toString().trim());

                    for(int i = 0;i < placeHashMap.size();i++)
                        if(placeHashMap.get(i).x == temp.x && placeHashMap.get(i).y == temp.y && i!=numid){
                            Toast.makeText(MainActivity.this, "修改失败，该地点已存在景点!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    placeHashMap.remove(numid);
                    placeHashMap.put(numid, temp);
                    WriteVex();
                    Paitting();
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });

        builder.show();

        }catch (Exception e){
            Toast.makeText(MainActivity.this, "请输入正确的景点编号!", Toast.LENGTH_SHORT).show();
        }
    }

    public void ClickInsert(View v) {
        final Boolean Hasspot = placeHashMap.size() > 0;
        String showtest = (Hasspot == true ? "下一步":"完成");

        final EditText edits = new EditText(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("请输入新增景区的信息");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog, null);
        builder.setView(view);

        final EditText name = (EditText) view.findViewById(R.id.name);
        final EditText info = (EditText) view.findViewById(R.id.info);
        final EditText x = (EditText) view.findViewById(R.id.x);
        final EditText y = (EditText) view.findViewById(R.id.y);

        builder.setPositiveButton(showtest, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Place temp = new Place();
                    temp.name = name.getText().toString().trim();
                    temp.name = temp.name.replaceAll(":","");
                    temp.name = temp.name.replaceAll("id","");
                    temp.name = temp.name.replaceAll("\n","");
                    temp.info = info.getText().toString().trim();
                    temp.info = temp.info.replaceAll(":","");
                    temp.info = temp.info.replaceAll("id","");
                    temp.info = temp.info.replaceAll("\n","");
                    temp.x = Integer.parseInt(x.getText().toString().trim());
                    temp.y = Integer.parseInt(y.getText().toString().trim());

                    placeHashMap.put(placeHashMap.size(), temp);
                    //以下hashmap已改动
                    WriteVex();
                    IsRead = true;
                    IsRead = true;
                    if(!Hasspot) {
                        writeTxtToFile("0\n",getSDPath() + "/SpotSystem/","m01.txt");
                        ReadEdge();
                        Paitting();

                        return;
                    }

                    //下面为选项框
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("选择连通的景点");

                    String[] hobby = new String[placeHashMap.size() - 1];
                    for (int i = 0; i < placeHashMap.size() - 1; i++)
                        hobby[i] = "" + i + "  " + placeHashMap.get(i).name;

                    final String[] hobbies = hobby;

                    builder.setMultiChoiceItems(hobbies, null, new DialogInterface.OnMultiChoiceClickListener() {
                        int[] link = new int[100];

                        public void WriteData(String marks) {
                            String filePath = getSDPath() + "/SpotSystem/";
                            String fileName = "data.txt";
                            writeTxtToFile(marks, filePath, fileName);
                        }

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                link[which] = 1;
                                String temp = new String();
                                for (int i = 0; i < 100; i++)
                                    if (link[i] == 1)
                                        temp = temp + i + "\n";
                                WriteData(temp);
                            }
                        }
                    });

                    builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                        public Boolean IsinArr(Integer t, ArrayList<Integer> s) {
                            for (int i = 0; i < s.size(); i++)
                                if (s.get(i) == t)
                                    return true;
                            return false;
                        }

                        public ArrayList<Integer> ReadData() {
                            File file = new File(dataName);
                            String temp = getFileContent(file);
                            String[] numbers = temp.split("\n");
                            ArrayList<Integer> ans = new ArrayList<>(numbers.length);

                            for (int i = 0; i < numbers.length; i++)
                                ans.add(Integer.parseInt(numbers[i]));

                            return ans;
                        }

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < placeHashMap.size() - 1; i++) {
                                if (placeHashMap.get(i).x == placeHashMap.get(placeHashMap.size() - 1).x && placeHashMap.get(i).y == placeHashMap.get(placeHashMap.size() - 1).y) {
                                    Toast.makeText(MainActivity.this, "添加失败，该地点已存在景点!", Toast.LENGTH_SHORT).show();
                                    placeHashMap.remove(placeHashMap.size() - 1);
                                    WriteVex();
                                    return;
                                }
                            }

                            ArrayList<Integer> links = ReadData();
                            ReadEdge();
                            String[] cows = tfmatrix.split("\n");
                            ArrayList<String> al = new ArrayList<>();
                            for (int i = 0; i < cows.length; i++)
                                al.add(cows[i]);
                            StringBuffer temp = new StringBuffer();
                            for (int i = 0; i < cows.length; i++) {
                                if (!IsinArr(i, links))
                                    temp.append('0');
                                else
                                    temp.append('1');
                                if (cows.length - 1 != i) {
                                    temp.append(',');
                                }
                            }

                            al.add(new String(temp));
                            tfmatrix = "";
                            for (int i = 0; i < al.size(); i++) {
                                tfmatrix = tfmatrix + al.get(i);
                                if (IsinArr(i, links))
                                    tfmatrix = tfmatrix + ",1\n";
                                else
                                    tfmatrix = tfmatrix + ",0\n";
                            }
                            WritetfMatrix();
                            ReadEdge();
                            Paitting();
                        }

                    });


                    builder.show();
                }catch(Exception e){
                    Toast.makeText(MainActivity.this, "输入格式错误!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void ClickSTpath(View v){
        if(!IsRead){
            Toast.makeText(MainActivity.this, "请先读入景区信息或插入景点!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("路线规划");
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.seput, null);
        builder.setView(view);
        final EditText sttext = (EditText)view.findViewById(R.id.start);
        final EditText entest = (EditText)view.findViewById(R.id.end);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int st = Integer.parseInt(sttext.getText().toString().trim());
                    int en = Integer.parseInt(entest.getText().toString().trim());

                    if(st>e||en>e||st<0||en<0||st==en)
                        throw new NumberFormatException();

                    String stpath = STpah(outputstring, e, n, st, en);
                    String[] datas = stpath.split("\n");
                    Paitting();
                    paint.setStrokeWidth(1);
                    paint.setColor(Color.GREEN);

                    String[] path = datas[0].split("[*]");

                    for (int i = 0; i < path.length - 1; i++) {
                        int id1 = Integer.parseInt(path[i]);
                        int id2 = Integer.parseInt(path[i + 1]);
                        canvas.drawLine(placeHashMap.get(id1).x, placeHashMap.get(id1).y, placeHashMap.get(id2).x, placeHashMap.get(id2).y, paint);
                    }

                    TextView output = (TextView) findViewById(R.id.output);
                    output.setText("总路程: " + datas[1]);
                    Toast.makeText(MainActivity.this, "已在图中绘制 " + st + " 到 " + en +" 的最优路径", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "请输入正确的景点编号!", Toast.LENGTH_SHORT).show();
                }
            }

        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });

        builder.show();
    }

    public native String TravelAll(String A,int e, int n, int st);
    public native String MSTree(String A,int e,int n);
    public native String STpah(String A,int e,int n,int st,int en);
}