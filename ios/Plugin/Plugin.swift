import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(FileDownloader)
public class FileDownloader: CAPPlugin {

    @objc func echo(_ call: CAPPluginCall) {
        let url = call.getString("url") ?? ""
        let filename = call.getString("filename") ?? ""
        let dir = call.getString("dir") ?? ""

        call.success([
            "url": url
        ])
    }
}
