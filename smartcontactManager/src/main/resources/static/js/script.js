

// Ensure the DOM is fully loaded
$(document).ready(function () {
    $("#toggle-btn").click(function () {
        const sidebar = $("#sidebar");
        const content = $("#content");

        // Toggle visibility classes
        sidebar.toggleClass("hidden");
        content.toggleClass("expanded");
    });
});


function search() {
  console.log("searching");

  let query = $("#search-input").val();

  if (query === "") {
    $(".search-result").hide();
  } else {
    console.log(query);

    // sending request to server
    let url = `http://localhost:8080/search/${query}`;

    fetch(url)
      .then((response) => response.json())
      .then((data) => {
        console.log(data);

        let text = `<div class='list-group'>`;

        data.forEach((contact) => {
          text += `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-item-action'>${contact.name}</a>`;
        });

        text += `</div>`;

        // ❗ FIXED: Added missing "." before "search-result"
        $(".search-result").html(text);
        $(".search-result").show();
      });
  }
}

//first request to server to create order

const paymentStart = () => {
  console.log("payment started");
  let amount =$("#payment_field").val();
  console.log(amount+"amount");
  if(amount=='' || amount == null){
    alert("amount is required");
  return;
  }

    // we will use AJAx to send request to the Server
$.ajax({
  url: '/user/create_order',
  data: JSON.stringify({ amount: amount, info: 'order_request' }),
  contentType: 'application/json',
  type: 'POST',
  dataType: 'json',
  success: function(response) {
    // invoked when success
    console.log(response);
    if(response.status=="created"){
      // open payment form
      let options ={
        key:'rzp_test_CnhZtQfiw22XZ8',
        amount:response.amount,
        currency:'INR',
        name:'Smart Contact Manager ',
        description :'Please Donate Us',
        image:"https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.nhrwwo.com%2Fdonate-us%2F&psig=AOvVaw2h9_ec8aBjl2ldYpEJEMVj&ust=1745004706789000&source=images&cd=vfe&opi=89978449&ved=0CBEQjRxqFwoTCMiulaDn34wDFQAAAAAdAAAAABAJ",
        order_id:response.id,
		handler:function(response){
			console.log(response.razorpay_payment_id);
      console.log("payment done");
      updatePaymentOnServer(response.razorpay_payment_id,response.razorpay_order_id,'PAID');
    
		}, "prefill": { //We recommend using the prefill parameter to auto-fill customer's contact information especially their phone number
      "name": "", //your customer's name
      "email": "",
      "contact": "" //Provide the customer's phone number for better conversion rates 
  },
  "notes": {
      "address": "Learn code with chai"
  },
  "theme": {
      "color": "#3399cc"
  }
        
      };
      let rzp = new Razorpay(options);
      rzp.on('payment.failed', function (response){
      console.log(response.error.code);
      console.log(response.error.description);
      console.log(response.error.source);
      console.log(response.error.step);
      console.log(response.error.reason);
      console.log(response.error.metadata.order_id);
      console.log(response.error.metadata.payment_id);
      alert("oops payment failed");
    });
      rzp.open();
    }
  },
  error: function(error) {
    console.log(error);
    alert("Something went wrong");
  }
});

};

//updating payment 
function updatePaymentOnServer(payment_id,order_id,PaymentStatus){

  $.ajax({
    url:"/user/update_order",
    data:JSON.stringify({payment_id,payment_id,order_id:order_id,PaymentStatus:PaymentStatus}),
    contentType:"application/json",
    type:"POST",
    dataType:"json",
    success:function(response){
      alert("congrated !! payment Successfull");
    },
    error:function(error){
      alert("you payment is successful we did not received, we will contact you as soon as possibe");
    },


  });

}

