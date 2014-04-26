
package hu.u_szeged.scannerpro.ui.fragments.views;


import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.model.exceptions.CommitToDatabaseFailedException;
import hu.u_szeged.scannerpro.model.exceptions.CustomerIDExistsException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;


/**
 * The fragment of the customer editor view
 */
public class EditCustomerFragment extends Fragment
{
	
	private Customer customer;
	private boolean preventLayoutOnStart = false;
	
	public EditCustomerFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.viewfragment_editcustomer, container, false);
		
		rootView.findViewById(R.id.buttonSave).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				try
				{
					if (customer == null)
					{
						Customer c = new Customer(((EditText) getView().findViewById(R.id.editTextCustomerId)).getText().toString());
						DAO.AddCustomer(c);
						customer = c;
					}
					
					customer.setName(((EditText) getView().findViewById(R.id.editTextCustomerName)).getText().toString());
					customer.setAddress(((EditText) getView().findViewById(R.id.editTextCustomerAddress)).getText().toString());
					customer.setMeterId(((EditText) getView().findViewById(R.id.editTextMeterId)).getText().toString());
					customer.setMeterType(((EditText) getView().findViewById(R.id.editTextMeterType)).getText().toString());
					
					DAO.Commit();
					
					getFragmentManager().popBackStack();
				}
				catch (IllegalArgumentException e)
				{
					new AlertDialog.Builder(getActivity())
							.setTitle("Hibás ügyfélazonosító")
							.setMessage("Az ügyfélazonosító megadása kötelezõ")
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									// do nothing
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
				}
				catch (CustomerIDExistsException e)
				{
					new AlertDialog.Builder(getActivity())
							.setTitle("Hibás ügyfélazonosító")
							.setMessage("Ezzel az azonosítóval már szerepel ügyfél az adatbázisban")
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									// do nothing
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
				} 
				catch (IllegalStateException e) 
				{
					new AlertDialog.Builder(getActivity())
							.setTitle("Adatbázis hiba")
							.setMessage("Az adatbázis nem áll készen")
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									// do nothing
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
					
					e.printStackTrace();
				} 
				catch (CommitToDatabaseFailedException e) 
				{
					new AlertDialog.Builder(getActivity())
							.setTitle("Adatbázis hiba")
							.setMessage("Sikertelen tranzakció")
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									// do nothing
								}
							})
							.setIcon(android.R.drawable.ic_dialog_alert)
							.show();
					
					e.printStackTrace();
				}
			}
		});
		
		rootView.findViewById(R.id.buttonDelete).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				new AlertDialog.Builder(getActivity())
						.setTitle("Ügyfél törlése")
						.setMessage("Biztosan törli ezt az ügyfelet?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								try
								{
									if (customer != null)
									{
										DAO.RemoveCustomer(customer).commit();
										setCustomer(null);
									}
									
									getFragmentManager().popBackStack();
								} 
								catch (IllegalStateException e) 
								{
									new AlertDialog.Builder(getActivity())
											.setTitle("Adatbázis hiba")
											.setMessage("Az adatbázis nem áll készen")
											.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which)
												{
													// do nothing
												}
											})
											.setIcon(android.R.drawable.ic_dialog_alert)
											.show();
									
									e.printStackTrace();
								} 
								catch (CommitToDatabaseFailedException e) 
								{
									new AlertDialog.Builder(getActivity())
											.setTitle("Adatbázis hiba")
											.setMessage("Sikertelen tranzakció")
											.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
												
												@Override
												public void onClick(DialogInterface dialog, int which)
												{
													// do nothing
												}
											})
											.setIcon(android.R.drawable.ic_dialog_alert)
											.show();
									
									e.printStackTrace();
								}
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								// do nothing
							}
						})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
			}
		});
		
		
		return rootView;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		if(!preventLayoutOnStart)
		{
			layoutCustomer();
		}
		
		preventLayoutOnStart = false;
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
			customer = DAO.FindCustomerById(savedInstanceState.getString("customer.id"));
		}
		
		preventLayoutOnStart = true;
	}
	
	
	/**
	 * @return the customer being edited (null if none created yet)
	 */
	public Customer getCustomer()
	{
		return customer;
	}
	
	
	/**
	 * @param customer the customer to edit (null to create a new one)
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
		
		if (customer == null)
		{
			((EditText) getView().findViewById(R.id.editTextCustomerId)).setText(null);
			((EditText) getView().findViewById(R.id.editTextCustomerId)).setEnabled(true);
			((EditText) getView().findViewById(R.id.editTextCustomerName)).setText(null);
			((EditText) getView().findViewById(R.id.editTextCustomerAddress)).setText(null);
			((EditText) getView().findViewById(R.id.editTextMeterId)).setText(null);
			((EditText) getView().findViewById(R.id.editTextMeterType)).setText(null);
		}
		else
		{
			((EditText) getView().findViewById(R.id.editTextCustomerId)).setText(customer.getId());
			((EditText) getView().findViewById(R.id.editTextCustomerId)).setEnabled(false);
			((EditText) getView().findViewById(R.id.editTextCustomerName)).setText(customer.getName());
			((EditText) getView().findViewById(R.id.editTextCustomerAddress)).setText(customer.getAddress());
			((EditText) getView().findViewById(R.id.editTextMeterId)).setText(customer.getMeterId());
			((EditText) getView().findViewById(R.id.editTextMeterType)).setText(customer.getMeterType());
		}
	}
}
