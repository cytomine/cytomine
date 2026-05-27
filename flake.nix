{
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShell = pkgs.mkShell {
          LD_LIBRARY_PATH = pkgs.lib.strings.makeLibraryPath [
            pkgs.stdenv.cc.cc.lib
            pkgs.zlib
          ];
          JAVA_HOME = pkgs.lib.strings.makeLibraryPath [ pkgs.jdk24 ];
          buildInputs = with pkgs; [
            chart-testing
            age
            fluxcd
            jdk24
            kubectl
            kubernetes-helm
            kustomize
            nodejs
            openssl
            postgresql
            python3
            sops
            uv
          ];
          shellHook = ''
            export KUBECONFIG=./.kube/config
          '';
        };
      }
    );
}
