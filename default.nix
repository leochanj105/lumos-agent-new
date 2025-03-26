{ pkgs ? import <nixpkgs> {} }:

let
  inherit (pkgs)
    lib
    maven
    jdk8
    aspectj;
in
maven.buildMavenPackage {
  pname = "lumos-agent";
  version = "0.0.1";

  src = ./.;

  mvnHash = "sha256-WvvtpkvyHnFGRZcD8XUkVY9mC4YgBZPLsWePv0jfPsI=";

  mvnParameters = lib.escapeShellArgs [
    "-T"
    "64"
    "clean"
    "install"
    "-U"
  ];

  nativeBuildInputs = [
    jdk8
    aspectj
  ];

  mvnJdk = jdk8;

  postInstall = ''
    mv target $out
  '';
}
