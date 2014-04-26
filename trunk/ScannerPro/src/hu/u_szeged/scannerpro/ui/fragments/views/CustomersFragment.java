
package hu.u_szeged.scannerpro.ui.fragments.views;


import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.ui.fragments.components.CustomerThumbnailFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;


/**
 * The fragment of the customer list view
 */
public class CustomersFragment extends Fragment
{
	private boolean started = false;
	private String filter = null;
	
	public CustomersFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.viewfragment_customers, container, false);
		
		rootView.findViewById(R.id.buttonNewCustomer).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				final FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.container, new EditCustomerFragment());
				ft.addToBackStack(null);
				ft.commit();
			}
		});
		
		((EditText) rootView.findViewById(R.id.editTextSearch)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				if (started)
				{
					filter = s.toString();
					
					if (filter != null && filter.equals(""))
					{
						filter = null;
					}
					
					layoutCustomers();
				}
			}
		});
		
		
		return rootView;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		
		layoutCustomers();
		
		started = true;
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop()
	{
		super.onStop();
		
		started = false;
	}
	
	/**
	 * Lays out the list of customers
	 */
	private void layoutCustomers()
	{
		LinearLayout panelCustomerList = (LinearLayout) getView().findViewById(R.id.panelCustomerList);
		
		panelCustomerList.removeAllViews();
		
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		if (filter != null)
		{
			filter = filter.toLowerCase();
		}
		
		for (Customer customer : DAO.GetCustomers())
		{
			if (filter == null
					|| (customer.getAddress() != null && customer.getAddress().toLowerCase().contains(filter))
					|| (customer.getId() != null && customer.getId().toLowerCase().contains(filter))
					|| (customer.getMeterId() != null && customer.getMeterId().toLowerCase().contains(filter))
					|| (customer.getMeterType() != null && customer.getMeterType().toLowerCase().contains(filter))
					|| (customer.getName() != null && customer.getName().toLowerCase().contains(filter)))
			{
				CustomerThumbnailFragment customerThumbnail = new CustomerThumbnailFragment();
				customerThumbnail.setCustomer(customer);
				ft.add(R.id.panelCustomerList, customerThumbnail);
			}
		}
		
		ft.commit();
	}
}
