package com.dicka.onlineshopping.springbootcore.controller;

import com.dicka.onlineshopping.springbootcore.dao.AccountsDao;
import com.dicka.onlineshopping.springbootcore.dao.ProductDao;
import com.dicka.onlineshopping.springbootcore.entity.Accounts;
import com.dicka.onlineshopping.springbootcore.entity.Product;
import com.dicka.onlineshopping.springbootcore.form.CustomerForm;
import com.dicka.onlineshopping.springbootcore.model.CartModelInfo;
import com.dicka.onlineshopping.springbootcore.model.CustomerModelInfo;
import com.dicka.onlineshopping.springbootcore.model.ProductModelInfo;
import com.dicka.onlineshopping.springbootcore.utils.Utils;
import com.dicka.onlineshopping.springbootcore.validator.CustomerFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    private final AccountsDao accountsDao;
    private final ProductDao productDao;

    @Autowired
    private CustomerFormValidator customerFormValidator;

    @Autowired
    public UserController(AccountsDao accountsDao, ProductDao productDao){
        this.accountsDao=accountsDao;
        this.productDao=productDao;
    }

    //@InitBinder binding
    @InitBinder
    public void myInitBinder(WebDataBinder dataBinder){
        Object target = dataBinder.getTarget();
        if(target == null){
            return;
        }
        System.out.println("Target = "+target);

        if(target.getClass() == CartModelInfo.class){

        }else if(target.getClass() == CustomerForm.class){
            dataBinder.setValidator(customerFormValidator);
        }
    }

    //home melihat data barang
    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public ModelAndView getHome(){
        ModelAndView view = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Accounts accounts = accountsDao.findAccountByUsername(auth.getName());
        view.addObject("activeClass", "active");
        view.addObject("title", "Home");
        view.addObject("who", "Hallo "+accounts.getUsername());
        view.setViewName("content/home");
        return view;
    }

    //cart template
    @RequestMapping(value = "/cart", method = RequestMethod.GET)
    public ModelAndView getDataCart(HttpServletRequest request){
        ModelAndView view = new ModelAndView();
        CartModelInfo myCart = Utils.getCartInSession(request);
        view.addObject("cartForm", myCart);
        view.addObject("amount", myCart.getAmountTotal());
        view.setViewName("content/cart");
        view.addObject("title", "Your Cart");
        return view;
    }

    //update quantity cart
    @RequestMapping(value = "/cart", method = RequestMethod.POST)
    public String updateQuantityInCart(HttpServletRequest request, Model model,
                                       @ModelAttribute("cartForm")
                                               CartModelInfo cartForm){

        CartModelInfo cartModelInfo = Utils.getCartInSession(request);
        cartModelInfo.updateQuantity(cartForm);
        return "redirect:/cart";
    }


    //menambahkan item ke cart
    @RequestMapping(value = "/addToCart", method = RequestMethod.GET)
    public String getCart(@RequestParam(value = "code", defaultValue = "")String code,
                                HttpServletRequest request){

        Product product = null;
        if(code !=null && code.length()>0){
            product = productDao.findProduct(Long.parseLong(code));
        }

        if(product!=null){
            CartModelInfo cartModelInfo = Utils.getCartInSession(request);
            ProductModelInfo productModelInfo = new ProductModelInfo(product);
            cartModelInfo.addProduct(productModelInfo, 1);
        }

        return "redirect:/cart";
    }

    //remove atau hapus barang di dalam cart
    @RequestMapping(value = "/removeCartItem")
    public String getRemoveCart(HttpServletRequest request, Model model,
                                @RequestParam(value = "code", defaultValue = "")String code){

        Product product=null;
        if(code!=null && code.length()>0){
            product=productDao.findProduct(Long.parseLong(code));
        }

        if(product!=null){
            CartModelInfo cartModelInfo = Utils.getCartInSession(request);

            ProductModelInfo productModelInfo = new ProductModelInfo(product);

            cartModelInfo.removeProduct(productModelInfo);
        }

        return "redirect:/cart";
    }

    //validation cart customer
    @RequestMapping(value = "/validationCartCustomer", method = RequestMethod.GET)
    public String getValidationCartCustomer(HttpServletRequest request, Model model){

        CartModelInfo cartModelInfo = Utils.getCartInSession(request);

        if(cartModelInfo.isEmpty()){
            return "redirect:/cart";
        }

        CustomerModelInfo customerModelInfo = cartModelInfo.getCustomerModelInfo();

        CustomerForm customerForm = new CustomerForm(customerModelInfo);

        model.addAttribute("customerForm", customerForm);

        return "";
    }
}
