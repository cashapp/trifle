//
//  ViewController.swift
//  CashSecuritySdk
//
//  Copyright (c) 2022 CashApp. All rights reserved.
//

import UIKit
import SecuritySdk

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        let version = LibraryVersion().complete()
        NSLog("Security SDK version: %@", version)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

}

