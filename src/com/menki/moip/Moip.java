package com.menki.moip;


import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

public class Moip extends Activity implements OnClickListener{
	private Handler guiThread;
	private Button buyButton;
	private EditText valueToSend;
	private WebView webView;
	
	public static final String tokenURL = "https://desenvolvedor.moip.com.br/sandbox/ws/alpha/EnviarInstrucao/Unica";
	public static final String redirectURL = "https://desenvolvedor.moip.com.br/sandbox/Instrucao.do?token=";
	
	public static final String startOfXml = "<EnviarInstrucao><InstrucaoUnica><Razao>Testando API do MoIP</Razao><IdProprio>12345</IdProprio><Valores><Valor moeda=\"BRL\">";
	public static final String endOfXml = "</Valor></Valores></InstrucaoUnica></EnviarInstrucao>";
	
	public final String token = "SEU TOKEN AQUI";
	public final String key = "SUA KEY AQUI";

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		
		setContentView(R.layout.main);
		findViews();
		setListeners();
	}
	

	private void findViews() {
		buyButton = (Button) findViewById(R.id.buy_button);	
		valueToSend = (EditText) findViewById(R.id.value);
		webView = (WebView) findViewById(R.id.web_view);
	}
		
	private void setListeners() {
		buyButton.setOnClickListener(this);		
	}
	
	public void redirectTo(String url){
		guiRedirectTo(webView, url);
	}
	
	private void guiRedirectTo(final WebView webView, final String url){
		guiThread.post(new Runnable(){
			public void run(){
				webView.loadUrl(url);
			}
		});
	}
	
	public void onClick(View v) {

		switch (v.getId()) {
		
		case R.id.buy_button:
			guiThread = new Handler();
			new Thread(new GetToken(Moip.this)).start();
			break;
		}

	}
	
	class GetToken implements Runnable {
		private final Moip moip;
			
		GetToken(Moip moip){
			this.moip = moip;
		}
		
		public void run() {		
			
			try {
				DefaultHttpClient client = new DefaultHttpClient();
			    
		   	    HttpPost post = new HttpPost(tokenURL);
		   	    
		   	    byte[] auth = (token + ":" + key).getBytes();
		   	    post.addHeader("Authorization", "Basic " + new String(Base64.encodeBytes(auth)));
		   	    StringEntity entity = new StringEntity(startOfXml + moip.valueToSend.getText().toString().trim() + endOfXml, "UTF-8");
		   	    entity.setContentType("application/x-www-formurlencoded");
		   	    post.setEntity(entity);
		   	    
				HttpResponse response = client.execute(post);
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(response.getEntity().getContent());
				NodeList nodeList = doc.getElementsByTagName("Token");
				String responseToken = nodeList.item(0).getTextContent();

				moip.redirectTo(redirectURL + responseToken);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
}
