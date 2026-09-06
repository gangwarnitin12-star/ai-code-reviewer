// package com.aicodereviewer;
// import
// org.springframework.web.bind.annotation.RestController;
// import
// org.springframework.web.bind.annotation.GetMapping;
// import
// org.springframework.web.bind.annotation.PostMapping;
// import
// org.springframework.web.bind.annotation.RequestBody;

// @RestController
// public class codeReviewController{
//     @GetMapping("/hello")
//     public String hello() {
//         return "Hello from AI Code Reviewer!";
//     }
//     @PostMapping("/webhook")
//     public String webhook(@RequestBody String payload){
//         System.out.println(payload);
//         return "webhook received!";

//     }
    
// }
package com.aicodereviewer;

import org.springframework.web.bind.annotation.*;

@RestController
public class codeReviewController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from AI Code Reviewer!";
    }

    @PostMapping("/webhook")
    public String webhook(@RequestBody String payload) {

        System.out.println("GitHub Webhook Payload:");
        System.out.println(payload);

        return "Webhook received!";
    }
}