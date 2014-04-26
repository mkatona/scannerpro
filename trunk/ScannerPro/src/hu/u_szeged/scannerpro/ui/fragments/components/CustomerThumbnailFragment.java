
package hu.u_szeged.scannerpro.ui.fragments.components;


import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.ui.fragments.views.EditCustomerFragment;
import hu.u_szeged.scannerpro.ui.fragments.views.ViewCustomerFragment;
import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * The fragment of the thumbnail of a customer
 */
public class CustomerThumbnailFragment extends Fragment
{
	
	private Customer customer;
	private boolean hideControls;
	
	public CustomerThumbnailFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.componentfragment_thumbnail_customer, container, false);
		
		rootView.findViewById(R.id.buttonEditCustomer).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				EditCustomerFragment view = new EditCustomerFragment();
				view.setCustomer(customer);
				ft.replace(R.id.container, view); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});
		
		rootView.findViewById(R.id.buttonViewCustomer).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				ViewCustomerFragment view = new ViewCustomerFragment();
				view.setCustomer(customer);
				ft.replace(R.id.container, view); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
	    super.onInflate(activity, attrs, savedInstanceState);

	    TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.CustomerThumbnailFragment);

	    setHideControls(a.getBoolean(R.styleable.CustomerThumbnailFragment_hide_controls, false));

	    a.recycle();
	}

	@Override
	public void onStart() {
		super.onStart();
		
		layoutCustomer();
		updateHideControls();
	}
	
	
	/**
	 * @return the customer being displayed
	 */
	public Customer getCustomer()
	{
		return customer;
	}
	
	
	/**
	 * @param customer the customer to display (not null)
	 */
	public void setCustomer(Customer customer)
	{
		this.customer = customer;
		
		layoutCustomer();
	}
	
	/**
	 * Sets whether the control buttons should be hidden
	 * 
	 * @param hideControls
	 */
	public void setHideControls(boolean hideControls)
	{
		this.hideControls = hideControls;
		
		updateHideControls();
	}
	
	private void updateHideControls()
	{
		if(getView() == null)
		{
			return;
		}
		
		if(hideControls)
		{
			getView().findViewById(R.id.linearLayoutControls).setVisibility(View.GONE);
			// TODO: data panel kiszélesítése
		}
		else
		{
			getView().findViewById(R.id.linearLayoutControls).setVisibility(View.VISIBLE);
		}
	}
	
	private void layoutCustomer()
	{
		if(getView() == null || customer == null)
		{
			return;
		}
		
		((TextView) getView().findViewById(R.id.textViewCustomerId)).setText(customer.getId());
		((TextView) getView().findViewById(R.id.textViewCustomerName)).setText(customer.getName());
		((TextView) getView().findViewById(R.id.textViewCustomerAddress)).setText(customer.getAddress());
	}
}
