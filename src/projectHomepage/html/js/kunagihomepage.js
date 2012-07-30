var kunagihp = {};

kunagihp.subscribe = function(baseUrl) {
    var email = prompt('Please enter your email for subscription');
    if (!email) return;
    window.location = baseUrl + email;
}

kunagihp.sharrre = function() {
    $('#shareme').sharrre({
        share: {
          googlePlus: true,
          facebook: true,
          twitter: true,
          digg: true,
          delicious: true,
          stumbleupon: true,
          linkedin: true
        },
        buttons: {
          googlePlus: {size: 'tall'},
          facebook: {layout: 'box_count'},
          twitter: {count: 'vertical'},
          digg: {type: 'DiggMedium'},
          delicious: {size: 'tall'},
          stumbleupon: {layout: '5'},
          linkedin: {counter: 'top'}
        }
      });
}