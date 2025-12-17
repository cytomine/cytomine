{
  inputs = {
    nixpkgs.url = "nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let pkgs = nixpkgs.legacyPackages.${system};
      in {
        devShell = pkgs.mkShell {
          LD_LIBRARY_PATH = pkgs.lib.strings.makeLibraryPath [ pkgs.stdenv.cc.cc.lib pkgs.zlib ];
          buildInputs = with pkgs;[
            chart-testing
            fluxcd
            kubectl
            kubernetes-helm
            kustomize
            openssl
            postgresql
            python3
            uv
          ];
          shellHook = ''
            export KUBECONFIG=./.kube/config
          '';
        };
      });
}
