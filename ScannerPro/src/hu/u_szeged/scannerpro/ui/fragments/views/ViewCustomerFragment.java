
package hu.u_szeged.scannerpro.ui.fragments.views;


import com.google.common.collect.Lists;

import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.ui.fragments.components.ReadingThumbnailFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * The fragment of the customer display view
 */
public class ViewCustomerFragment extends Fragment
{
	
	private Customer customer;
	
	public ViewCustomerFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.viewfragment_viewcustomer, container, false);
		return rootView;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		layoutCustomer();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		if(customer != null)
		{
			outState.putString("customer.id", customer.getId());
		}
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		if(savedInstanceState == null)
		{
			return;
		}
		
		if(savedInstanceState.containsKey("customer.id"))
		{
			setCustomer(DAO.FindCustomerById(savedInstanceState.getString("customer.id")));
		}
		else
		{
			setCustomer(null);
		}
	}
	
	
	/**
	 * @return the customer being edited
	 */
	public Customer getCustomer()
	{
		return customer;
	}
	
	
	/**
	 * @param customer the customer to edit (not null)
	 */
	public void setCustomer(Customer customer)
	{
		this.customer = customer;
		
		layoutCustomer();
	}
	
	private void layoutCustomer()
	{
		if (getView() == null)
		{
			return;
		}
		
		((TextView) getView().findViewById(R.id.textViewCustomerId)).setText("Ügyfélazonosító: " + customer.getId());
		((TextView) getView().findViewById(R.id.textViewCustomerName)).setText("Név: " + customer.getName());
		((TextView) getView().findViewById(R.id.textViewCustomerAddress)).setText("Szolgáltatási hely: " + customer.getAddress());
		((TextView) getView().findViewById(R.id.textViewMeterId)).setText("Mérõóra azonosító: " + customer.getMeterId());
		((TextView) getView().findViewById(R.id.textViewMeterType)).setText("Mérõóra típus: " + customer.getMeterType());
		
		LinearLayout readingsPanel = (LinearLayout) getView().findViewById(R.id.linearLayoutPreviousReadings);
		readingsPanel.removeAllViews();
		
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		for (Reading reading : Lists.reverse(customer.getReadings()))
		{
			ReadingThumbnailFragment readingThumbnail = new ReadingThumbnailFragment();
			readingThumbnail.setReading(reading);
			readingThumbnail.setHideCustomer(true);
			ft.add(R.id.linearLayoutPreviousReadings, readingThumbnail);
		}
		
		ft.commit();
	}
}
