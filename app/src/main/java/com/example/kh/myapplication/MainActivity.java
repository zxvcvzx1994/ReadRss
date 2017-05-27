package com.example.kh.myapplication;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity implements ResultCallback{

    private static final String TAG = "vocongivn";
    private MyFragment myFragment;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null){
            myFragment = new MyFragment();
            getSupportFragmentManager().beginTransaction().add(myFragment,"MyFragment").commit();
        }else{
            myFragment  = (MyFragment) getSupportFragmentManager().findFragmentByTag("MyFragment");
        }
        myFragment.StartTask();

    }

    @Override
    public void OnPreExcute() {

    }

    @Override
    public void OnPostExcute(ArrayList<HashMap<String, String>> arrayList) {
        Log.i(TAG, "OnPostExcute: "+arrayList);
        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new MyAdapter(this, arrayList));
    }

    public static class MyFragment extends Fragment{
        MyAsynTask myAsynTask;
        ResultCallback resultCallback;
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setRetainInstance(true);

        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            resultCallback = (ResultCallback) context;
            if(myAsynTask!=null){
                myAsynTask.OnAttach(resultCallback);
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            resultCallback = null;
            if(myAsynTask!=null)
                myAsynTask.OnDetach();
        }

        public void StartTask(){
            if(myAsynTask!=null){
                myAsynTask.cancel(true);
            }else{
                myAsynTask = new MyAsynTask(resultCallback);
                myAsynTask.execute("http://feeds.feedburner.com/techcrunch/android?format=xml");
            }

        }
    }


    public static class MyAsynTask extends AsyncTask<String, Integer, ArrayList<HashMap<String,String>>>{
        ResultCallback context=null;
    public  MyAsynTask(ResultCallback context){
      OnAttach(context);
    }

    public void OnAttach(ResultCallback context){
        this.context = context;
    }

    public void OnDetach(){
        context =null;
    }
        private static final String TAG ="BUG ERROR" ;
        private HttpURLConnection connection;
        private URL url;
        private InputStream inputStream;

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... params) {
            ArrayList<HashMap<String,String>> arrayList =new ArrayList<HashMap<String, String>>();
            try {
                url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                inputStream = connection.getInputStream();
               arrayList= processXML(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> hashMaps) {
          //  Log.i(TAG, "onPostExecute+HASKMAP: "+hashMaps);
            if(context!=null)
                context.OnPostExcute(hashMaps);
        }

        @Override
        protected void onPreExecute() {
            if(context!=null)
                context.OnPreExcute();
        }

        private ArrayList<HashMap<String,String>> processXML(InputStream inputStream) throws Exception {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.parse(inputStream);
            Element rootElement = xmlDocument.getDocumentElement();
            Log.i(TAG, "processXML: "+rootElement.getTagName());
            NodeList itemslist = rootElement.getElementsByTagName("item");
            Log.i(TAG, "processXML: "+itemslist.getLength());
            int count=0;
            HashMap<String, String> hashMap=null;
            ArrayList<HashMap<String,String>> arrayList = new ArrayList<HashMap<String, String>>();
            for(int i = 0 ; i< itemslist.getLength(); i++){
                Node nodeitem= itemslist.item(i);
                NodeList nodelistitem = nodeitem.getChildNodes();
                hashMap = new HashMap<String,String>();
                //
                for(int j = 0; j<nodelistitem.getLength(); j++){
                    Node childnodeitem = nodelistitem.item(j);

                    if(childnodeitem.getNodeName().equalsIgnoreCase("title")){
                        hashMap.put("title",childnodeitem.getTextContent());
                        Log.i(TAG, "processXML: "+childnodeitem.getTextContent());
                    }
                    if(childnodeitem.getNodeName().equalsIgnoreCase("pubDate")){
                        hashMap.put("pubDate",childnodeitem.getTextContent());
                        Log.i(TAG, "processXML+pubdate: "+childnodeitem.getTextContent());
                    }
                    if(childnodeitem.getNodeName().equalsIgnoreCase("description")){
                       Log.i(TAG, "processXML+description: "+childnodeitem.getTextContent());
                        hashMap.put("description", childnodeitem.getTextContent());
                    }

                    if(childnodeitem.getNodeName().equalsIgnoreCase("media:thumbnail")){
                     count++;
                        if(count==2){
                            hashMap.put("imageURL",childnodeitem.getAttributes().item(0).getTextContent());
                            Log.i(TAG, "processXML+attribute"+childnodeitem.getAttributes().item(0).getTextContent());
                       }
                    }

                }
                //
                if(hashMap!=null && !hashMap.isEmpty()){
                    arrayList.add(i,hashMap);
                }

                count=0;
              //  Log.i(TAG, "processXML: "+   itemslist.item(i).getNodeName());
            }
            return arrayList;
        }
    }



class MyAdapter extends BaseAdapter{
    Context context;
    ArrayList<HashMap<String,String>> array = new ArrayList<HashMap<String, String>>();
    private LayoutInflater inflater;

    public MyAdapter(Context context, ArrayList<HashMap<String,String>> array){
        this.array = array;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Viewholder holder = null;
        if(view==null){
            view = inflater.inflate(R.layout.list_view, parent,false);
            holder = new Viewholder(view);
            view.setTag(holder);
        }else holder= (Viewholder) view.getTag();
        HashMap<String,String> hashMap = array.get(position);
        holder.title.setText( hashMap.get("title"));
        holder.description.setText(hashMap.get("description"));
        Log.i(TAG, "getView: "+hashMap.get("imageURL"));
       Picasso.with(context).load(hashMap.get("imageURL")).into(holder.img);
        holder.date.setText(hashMap.get("pubDate"));
        return view;
    }
}
}
  interface  ResultCallback{
    void OnPreExcute();
    void OnPostExcute(ArrayList<HashMap<String, String>> arrayList);
}
 class Viewholder{
    TextView title;
    TextView description;
    ImageView img;
     TextView date;
    public Viewholder(View view){
        title  = (TextView) view.findViewById(R.id.txttitle);
        description  = (TextView) view.findViewById(R.id.txtDescription);
        img = (ImageView) view.findViewById(R.id.img);
        date = (TextView) view.findViewById(R.id.txtdate);
    }
}