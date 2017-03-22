package pl.edu.pja.sum.raspberrypiled;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener
{
	//Przechowuje odniesienie do ciągu identyfikującego usługę bluetooth
	private static final UUID SPP_UID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothAdapter btAdapter; //Przechowuje odniesienie do funkcjonalności zwiazaniej z urządzeniem bluetooth

	private EditText btAddress; //Przechowuje odniesienie do kontrolki służącej do wpisywania adresu urządzenia
	private ToggleButton btConnect; //Przechowuje odniesienie do przycisku "Połącz"/"Rozłącz"
	private ImageButton btLight; //Przechowuje odniesienie do przycisku z żarówką
	private boolean lightsOn = false; //Przechowuje stan naszej diody

	private BluetoothSocket socket; //Przechowuje odniesienie do połaczenia z innym urządzeniem
	private OutputStream out; //Przechowuje odniesienie do strumienia przesyłajacego dane

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btAdapter = BluetoothAdapter.getDefaultAdapter(); //Przypisanie domyślnego urządzenia bluetooth

		btAddress = (EditText) findViewById(R.id.bt_address); //Przypisanie komponentu do wpisywania tekstu o id bt_address

		btConnect = (ToggleButton) findViewById(R.id.bt_connect); //Przypisanie przełącznika o id bt_connect
		btConnect.setOnCheckedChangeListener(this); //Ustawienie nasłuchiwania zmiany stanu przycisku na ten obiekt

		btLight = (ImageButton) findViewById(R.id.bt_led); //Przypisanie przycisku o id bt_address
		btLight.setOnClickListener(this); //Ustawienie nasłuchiwania kliknięcia przycisku na ten obiekt
		btLight.setEnabled(false); //Tymczasowe wyłączenie przycisku
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		try
		{
			polacz_rozlacz(isChecked);
		}
		catch(IOException e)
		{
			//jeśli coś się nie powiedzie, to zgłaszamy bład.
			Toast.makeText(this, "Wystąpił jakś problem :( " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v)
	{
		try
		{
			zapal_zgas(!lightsOn);
			lightsOn = !lightsOn;
		}
		catch(IOException e)
		{
			//jeśli coś się nie powiedzie, to zgłaszamy bład
			Toast.makeText(this, "Nie udało się wysłać informacji :(", Toast.LENGTH_LONG).show();
		}
	}

	public void polacz_rozlacz(boolean polacz) throws IOException
	{
		//w zależności czy chcemy się połączyczyć czy rozłączyć wykonujemy inny fragment kodu
		if(polacz)
		{
			//wybieramy urządzenie po jego adresie, który wpiszemy w aplikacji
			BluetoothDevice raspberry = btAdapter.getRemoteDevice(String.valueOf(btAddress.getText()));

			//próbujemy nawiązać połączenie z uługą
			socket = raspberry.createRfcommSocketToServiceRecord(SPP_UID);
			socket.connect();
			out = socket.getOutputStream(); //otwieramy strumień do wysyłania danych

			btLight.setEnabled(true); //włączamy nasz przycisk z żarówką
		}
		else
		{
			out.close(); //zamyamy stumień do wysyłania danych
			socket.close(); //kończymy połączenie

			btLight.setEnabled(false); //wyłączamy przycisk z żarówką}
		}
	}

	public void zapal_zgas(boolean zapal) throws IOException
	{
		//w zależności czy chcemy zgasić czy zapalić diodę LED wykonujemy inny fragment kodu
		if(zapal)
		{
			out.write('1');

			btLight.setImageDrawable(getResources().getDrawable(R.drawable.ledon)); //zmieniamy wygląd żarówki
		}
		else
		{
			//wysyłamy 0 - co będzie oznaczało ze chcemy zgasić diodę
			out.write('0');

			btLight.setImageDrawable(getResources().getDrawable(R.drawable.ledoff)); //zmieniamy wygląd żarówki
		}
	}
}
