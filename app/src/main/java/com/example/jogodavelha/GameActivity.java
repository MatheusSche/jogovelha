package com.example.jogodavelha;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GameActivity extends AppCompatActivity {
    private ImageButton[][] gameboard = new ImageButton[3][3];
    private File arquivoFoto = null;
    private Bitmap selfieplayer1 = null;
    private Bitmap selfieplayer2 = null;
    private ImageView imagem;
    private ImageButton button_player_1 = null;
    private ImageButton button_player_2 = null;
    private final int CAMERA = 3;
    private int player = 0;
    private final int PERMISSAO_REQUEST = 2;
    private int jogada = 0;
    private Bitmap bitmap_n1 =null;
    private Bitmap bitmap_n2 =null;

    //check movment
    private enum TURN {
        CROSS, CIRCLE
    };

    private TURN turn;
    private Boolean winX = false;
    private Boolean winO = false;
    private Boolean NotWin = false;

    @BindView(R.id.txt_turn)
    TextView txtTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        this.button_player_1 = (ImageButton) findViewById(R.id.player1);
        this.button_player_2 = (ImageButton) findViewById(R.id.player2);
        this.bitmap_n1 = (Bitmap) ((BitmapDrawable)this.button_player_1.getDrawable()).getBitmap();
        this.bitmap_n2 = (Bitmap) ((BitmapDrawable)this.button_player_2.getDrawable()).getBitmap();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSAO_REQUEST);
            }
        }
        turn = TURN.CROSS;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String index = "index_" + i + j;
                int ID = getResources().getIdentifier(index, "id", getPackageName());
                gameboard[i][j] = findViewById(ID);
            }
        }

    }

    @OnClick(
            {
                    R.id.index_00,
                    R.id.index_01,
                    R.id.index_02,
                    R.id.index_10,
                    R.id.index_11,
                    R.id.index_12,
                    R.id.index_20,
                    R.id.index_21,
                    R.id.index_22,
            }
    )
    public void clickGameBoard(View view) {
        if (selfieplayer1 == null || selfieplayer2 == null) {
            txtTurn.setText("SELECIONE AS SELFIES");
            return;
        }
        ImageButton button = null;
        ImageButton imageButtonOption = ((ImageButton) view);
        jogada = jogada + 1;
        String tag_value = imageButtonOption.getTag().toString();
        if (!tag_value.equals("")) {
            return;
        }

        if (turn == TURN.CROSS) {
            turn = TURN.CIRCLE;
            imageButtonOption.setImageBitmap(selfieplayer1);
            imageButtonOption.setTag("X");

            txtTurn.setText("VEZ DO JOGADOR P2");
        } else if (turn == TURN.CIRCLE) {
            turn = TURN.CROSS;
            imageButtonOption.setImageBitmap(selfieplayer2);
            imageButtonOption.setTag("O");
            txtTurn.setText("VEZ DO JOGADOR P1");
        }

        imageButtonOption.setEnabled(false);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("RESULTADO DO JOGO");
        dialog.setCancelable(true);
        if (checkResults()) {
            if (winX) {
                dialog.setMessage("JOGADOR 1 GANHOU");
                dialog.create().show();
            } else if (winO) {
                dialog.setMessage("JOGADOR 2 GANHOU");
                dialog.create().show();
            } else if (NotWin) {
                dialog.setMessage("EMPATE");
                dialog.create().show();
            }
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    button = gameboard[i][j];
                    button.setEnabled(false);

                }
            }
        }
    }

    private void changeColor(ImageButton q1, ImageButton q2, ImageButton q3) {

        int pad = 25;
        int pad2 = 25;
        q1.setBackgroundColor(Color.parseColor("#4CAF50"));
        q1.setPadding(pad, pad2, pad, pad2);
        q2.setBackgroundColor(Color.parseColor("#4CAF50"));
        q2.setPadding(pad, pad2, pad, pad2);
        q3.setBackgroundColor(Color.parseColor("#4CAF50"));
        q3.setPadding(pad, pad2, pad, pad2);


    }

    private Boolean checkResults() {

        for (int i = 0; i < 3; i++) {
            // horizontal
            if (gameboard[i][0].getTag().toString().equals("X") &&
                    gameboard[i][1].getTag().toString().equals("X") &&
                    gameboard[i][2].getTag().toString().equals("X")) {
                winX = true;
                changeColor(gameboard[i][0], gameboard[i][1], gameboard[i][2]);
                return winX;
            }
            if (gameboard[i][0].getTag().toString().equals("O") &&
                    gameboard[i][1].getTag().toString().equals("O") &&
                    gameboard[i][2].getTag().toString().equals("O")) {
                winO = true;
                changeColor(gameboard[i][0], gameboard[i][1], gameboard[i][2]);
                return winO;
            }
            //VERTICAL
            if (gameboard[0][i].getTag().toString().equals("X") &&
                    gameboard[1][i].getTag().toString().equals("X") &&
                    gameboard[2][i].getTag().toString().equals("X")) {
                winX = true;
                changeColor(gameboard[0][i], gameboard[1][i], gameboard[2][i]);

                return winX;
            }
            if (gameboard[0][i].getTag().toString().equals("O") &&
                    gameboard[1][i].getTag().toString().equals("O") &&
                    gameboard[2][i].getTag().toString().equals("O")) {
                winO = true;
                changeColor(gameboard[0][i], gameboard[1][i], gameboard[2][i]);

                return winO;
            }

            //DIAGONAL
            if (gameboard[0][0].getTag().toString().equals("X") &&
                    gameboard[1][1].getTag().toString().equals("X") &&
                    gameboard[2][2].getTag().toString().equals("X")) {
                winX = true;
                changeColor(gameboard[0][0], gameboard[1][1], gameboard[2][2]);

                return winX;
            }
            if (gameboard[0][0].getTag().toString().equals("O") &&
                    gameboard[1][1].getTag().toString().equals("O") &&
                    gameboard[2][2].getTag().toString().equals("O")) {
                winO = true;
                changeColor(gameboard[0][0], gameboard[1][1], gameboard[2][2]);

                return winO;
            }
            if (gameboard[0][2].getTag().toString().equals("X") &&
                    gameboard[1][1].getTag().toString().equals("X") &&
                    gameboard[2][0].getTag().toString().equals("X")) {
                winX = true;
                changeColor(gameboard[0][2], gameboard[1][1], gameboard[2][0]);

                return winX;
            }
            if (gameboard[0][2].getTag().toString().equals("O") &&
                    gameboard[1][1].getTag().toString().equals("O") &&
                    gameboard[2][0].getTag().toString().equals("O")) {
                winO = true;
                changeColor(gameboard[0][2], gameboard[1][1], gameboard[2][0]);
                return winO;
            }
        }

        if (jogada == 9) {
            NotWin = true;
            return NotWin;
        }

        return false;
    }

    private File criaArquivo() throws IOException {
        String timeStamp = new
                SimpleDateFormat("yyyyMMdd_Hhmmss").format(
                new Date());
        File pasta = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imagem = new File(pasta.getPath() + File.separator
                + "JPG_" + timeStamp + ".jpg");
        return imagem;
    }

    public void tirarFoto(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                arquivoFoto = criaArquivo();
            } catch (IOException ex) {
                mostraAlerta(getString(R.string.erro), getString(
                        R.string.erro_salvando_foto));
            }
            if (arquivoFoto != null) {
                Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                        getBaseContext().getApplicationContext().getPackageName() +
                                ".provider", arquivoFoto);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                switch (view.getId()) {
                    case (R.id.player1):
                        player = 1;
                        break;
                    case (R.id.player2):
                        player = 2;
                        break;
                }

                startActivityForResult(takePictureIntent, CAMERA);
            }
        }

    }

    public void limparJogo(View view) {
        ImageButton button = null;
        jogada = 0;
        txtTurn.setText("");
        selfieplayer1 = null;
        selfieplayer2 = null;
        int pad = 30;

        this.button_player_1.setImageBitmap(this.bitmap_n1);
        this.button_player_2.setImageBitmap(this.bitmap_n2);
        this.button_player_1.setPadding(pad, pad, pad, pad);
        this.button_player_2.setPadding(pad, pad, pad, pad);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {

                button = gameboard[i][j];
                button.setTag("");
                button.setImageBitmap(null);
                button.setEnabled(true);
                button.setBackgroundColor(Color.parseColor("#207bba"));
                button.setPadding(0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA) {
            Bitmap selfie = BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath());

            switch (player) {
                case 1:
                    this.button_player_1.setImageBitmap(selfie);
                    this.button_player_1.setPadding(0, 0, 0, 0);
                    selfieplayer1 = selfie;
                    break;
                case 2:
                    this.button_player_2.setImageBitmap(selfie);
                    this.button_player_2.setPadding(0, 0, 0, 0);
                    selfieplayer2 = selfie;
                    break;

            }
        }


    }

    private void mostraAlerta(String titulo, String mensagem) {
        android.app.AlertDialog alertDialog = new
                android.app.AlertDialog.Builder(GameActivity.this).create();
        alertDialog.setTitle(titulo);
        alertDialog.setMessage(mensagem);
        alertDialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}
