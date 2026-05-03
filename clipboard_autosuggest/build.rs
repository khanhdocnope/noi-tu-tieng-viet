fn main() {
    if std::env::var("CARGO_CFG_TARGET_OS").unwrap_or_default() == "windows" {
        // Dùng CARGO_MANIFEST_DIR để đảm bảo đường dẫn tuyệt đối
        let dir = std::env::var("CARGO_MANIFEST_DIR").unwrap_or_default();
        let ico = format!("{dir}\\assets\\icon.ico");

        println!("cargo:rerun-if-changed={ico}");
        println!("cargo:rerun-if-changed=assets/icon.ico");

        let mut res = winres::WindowsResource::new();
        res.set_icon(&ico);
        match res.compile() {
            Ok(_) => eprintln!("cargo:warning=Icon embedded OK: {ico}"),
            Err(e) => eprintln!("cargo:warning=winres error: {e}"),
        }
    }
}
