
package hu.u_szeged.scannerpro.ui.activities;


import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.ui.fragments.views.MainFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		DAO.Init(this);
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.container, new MainFragment());
			//ft.addToBackStack(null);
			ft.commit();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		DAO.Destroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
