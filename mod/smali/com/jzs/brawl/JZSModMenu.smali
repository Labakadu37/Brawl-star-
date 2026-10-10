.class public final Lcom/jzs/brawl/JZSModMenu;
.super Ljava/lang/Object;
.source "JZSModMenu.java"


# static fields
.field private static final COL_3D_HIGHLIGHT:I = 0x20ffffff

.field private static final COL_3D_SHADOW:I = -0xf2f2e1

.field private static final COL_ACCENT:I = -0x16baa0

.field private static final COL_BG_CARD:I = -0xe9dec2

.field private static final COL_BG_DARK:I = -0xe5e5d2

.field private static final COL_BG_HEADER:I = -0xf5f5e6

.field private static final COL_BG_PANEL:I = -0xfe5e5d2

.field private static final COL_BG_SIDEBAR:I = -0xf0cba0

.field private static final COL_BORDER:I = 0x30ffffff

.field private static final COL_BTN_BLUE:I = -0xde690d

.field private static final COL_BTN_GREEN:I = -0xff339a

.field private static final COL_BTN_ORANGE:I = -0x94cb

.field private static final COL_BTN_RED:I = -0x16baa0

.field private static final COL_BTN_YELLOW:I = -0x3ef9

.field private static final COL_OFF:I = -0x84786c

.field private static final COL_ON:I = -0xff339a

.field private static final COL_OVERLAY_DIM:I = -0x68000000

.field private static final COL_SIDEBAR_SEL:I = -0xe5e5c0

.field private static final COL_TAB_SEL:I = 0x40e94560

.field private static final COL_TEXT:I = -0x1b1b1c

.field private static final COL_TEXT_DIM:I = -0x84786c

.field static final FEATURES:[[Ljava/lang/String;

.field private static final GROUPS:[Ljava/lang/String;

.field private static final GROUP_COLORS:[I

.field static final SLIDERS:[[Ljava/lang/String;

.field private static sActivity:Landroid/app/Activity;

.field private static sAttached:Z

.field private static sBody:Landroid/widget/LinearLayout;

.field private static sCurrentGroup:Ljava/lang/String;

.field private static sCurrentSubGroup:Ljava/lang/String;

.field private static sDimBg:Landroid/view/View;

.field private static sFab:Landroid/widget/FrameLayout;

.field private static sFabText:Landroid/widget/TextView;

.field private static sHandler:Landroid/os/Handler;

.field private static sHue:F

.field private static sPanel:Landroid/widget/FrameLayout;

.field private static sRainbow:Ljava/lang/Runnable;

.field private static sRootFrame:Landroid/widget/FrameLayout;

.field private static sSettingLines:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field private static sSidebar:Landroid/widget/LinearLayout;

.field private static sSliderViews:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "[",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation
.end field

.field private static sSubTabs:Landroid/widget/LinearLayout;

.field private static sToggleViews:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "[",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .registers 17

    .line 51
    const/16 v0, 0x13

    new-array v0, v0, [[Ljava/lang/String;

    const-string v1, "Aimbot"

    const-string v2, "mod_aimbot"

    const-string v3, "Battle"

    const-string v4, "Combat"

    filled-new-array {v1, v2, v3, v4}, [Ljava/lang/String;

    move-result-object v1

    const/4 v2, 0x0

    aput-object v1, v0, v2

    const-string v1, "Auto Aim"

    const-string v5, "mod_autoaim"

    filled-new-array {v1, v5, v3, v4}, [Ljava/lang/String;

    move-result-object v1

    const/4 v5, 0x1

    aput-object v1, v0, v5

    const-string v1, "Auto Fire"

    const-string v6, "mod_autofire"

    filled-new-array {v1, v6, v3, v4}, [Ljava/lang/String;

    move-result-object v1

    const/4 v6, 0x2

    aput-object v1, v0, v6

    const-string v1, "Wall Hack"

    const-string v7, "mod_wallhack"

    filled-new-array {v1, v7, v3, v4}, [Ljava/lang/String;

    move-result-object v1

    const/4 v4, 0x3

    aput-object v1, v0, v4

    const-string v1, "Speed Hack"

    const-string v7, "mod_speedhack"

    const-string v8, "Utility"

    filled-new-array {v1, v7, v3, v8}, [Ljava/lang/String;

    move-result-object v1

    const/4 v7, 0x4

    aput-object v1, v0, v7

    const-string v1, "God Mode"

    const-string v7, "mod_godmode"

    filled-new-array {v1, v7, v3, v8}, [Ljava/lang/String;

    move-result-object v1

    const/4 v7, 0x5

    aput-object v1, v0, v7

    const-string v1, "Unlimited Ammo"

    const-string v7, "mod_unlimitedammo"

    filled-new-array {v1, v7, v3, v8}, [Ljava/lang/String;

    move-result-object v1

    const/4 v7, 0x6

    aput-object v1, v0, v7

    const-string v1, "Auto Dodge"

    const-string v7, "mod_autododge"

    const-string v8, "Automation"

    filled-new-array {v1, v7, v3, v8}, [Ljava/lang/String;

    move-result-object v1

    const/4 v7, 0x7

    aput-object v1, v0, v7

    const-string v1, "Auto Play"

    const-string v7, "mod_autoplay"

    filled-new-array {v1, v7, v3, v8}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0x8

    aput-object v1, v0, v7

    const-string v1, "mod_nocooldown"

    const-string v7, "Exploits"

    const-string v8, "No Cooldown"

    filled-new-array {v8, v1, v3, v7}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0x9

    aput-object v1, v0, v7

    const-string v1, "Zoom Hack"

    const-string v7, "mod_zoomhack"

    const-string v8, "Visuals"

    const-string v9, "Camera"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xa

    aput-object v1, v0, v7

    const-string v1, "Show HP"

    const-string v7, "mod_showhp"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xb

    aput-object v1, v0, v7

    const-string v1, "No Fog"

    const-string v7, "mod_nofog"

    const-string v9, "Effects"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xc

    aput-object v1, v0, v7

    const-string v1, "Custom Skins"

    const-string v7, "mod_customskins"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xd

    aput-object v1, v0, v7

    const-string v1, "Rainbow Mode"

    const-string v7, "mod_rainbow"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xe

    aput-object v1, v0, v7

    const-string v1, "FPS Counter"

    const-string v7, "mod_fpscounter"

    const-string v9, "HUD"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0xf

    aput-object v1, v0, v7

    const-string v1, "Debug Info"

    const-string v7, "mod_debuginfo"

    filled-new-array {v1, v7, v8, v9}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0x10

    aput-object v1, v0, v7

    const-string v1, "Anti Ban"

    const-string v7, "mod_antiban"

    const-string v9, "Misc"

    const-string v10, "Security"

    filled-new-array {v1, v7, v9, v10}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0x11

    aput-object v1, v0, v7

    const-string v1, "Spoof Device"

    const-string v7, "mod_spoofdevice"

    filled-new-array {v1, v7, v9, v10}, [Ljava/lang/String;

    move-result-object v1

    const/16 v7, 0x12

    aput-object v1, v0, v7

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->FEATURES:[[Ljava/lang/String;

    .line 74
    new-array v0, v4, [[Ljava/lang/String;

    const-string v10, "Speed Value"

    const-string v11, "val_speed"

    const-string v12, "Battle"

    const-string v13, "50"

    const-string v14, "300"

    const-string v15, "10"

    const-string v16, "100"

    filled-new-array/range {v10 .. v16}, [Ljava/lang/String;

    move-result-object v1

    aput-object v1, v0, v2

    const-string v10, "Zoom Level"

    const-string v11, "val_zoom"

    const-string v12, "Visuals"

    const-string v13, "50"

    const-string v14, "500"

    const-string v15, "10"

    const-string v16, "100"

    filled-new-array/range {v10 .. v16}, [Ljava/lang/String;

    move-result-object v1

    aput-object v1, v0, v5

    const-string v10, "FPS Limit"

    const-string v11, "val_fps"

    const-string v12, "Visuals"

    const-string v13, "30"

    const-string v14, "120"

    const-string v15, "10"

    const-string v16, "60"

    filled-new-array/range {v10 .. v16}, [Ljava/lang/String;

    move-result-object v1

    aput-object v1, v0, v6

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->SLIDERS:[[Ljava/lang/String;

    .line 81
    filled-new-array {v3, v8, v9}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    .line 82
    const v0, -0xde690d

    const/16 v1, -0x3ef9

    const v2, -0x16baa0

    filled-new-array {v2, v0, v1}, [I

    move-result-object v0

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->GROUP_COLORS:[I

    .line 95
    sput-object v3, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    .line 96
    const-string v0, ""

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    .line 97
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sToggleViews:Ljava/util/ArrayList;

    .line 98
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sSliderViews:Ljava/util/ArrayList;

    .line 99
    new-instance v0, Landroid/os/Handler;

    invoke-static {}, Landroid/os/Looper;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sHandler:Landroid/os/Handler;

    .line 101
    const/4 v0, 0x0

    sput v0, Lcom/jzs/brawl/JZSModMenu;->sHue:F

    return-void
.end method

.method private constructor <init>()V
    .registers 1

    .line 103
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method static synthetic access$000(Landroid/app/Activity;)V
    .registers 1

    .line 25
    invoke-static {p0}, Lcom/jzs/brawl/JZSModMenu;->build(Landroid/app/Activity;)V

    return-void
.end method

.method static synthetic access$100()V
    .registers 0

    .line 25
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->togglePanel()V

    return-void
.end method

.method static synthetic access$1000()V
    .registers 0

    .line 25
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshToggleStates()V

    return-void
.end method

.method static synthetic access$1100(Ljava/lang/String;I)I
    .registers 2

    .line 25
    invoke-static {p0, p1}, Lcom/jzs/brawl/JZSModMenu;->getIntPref(Ljava/lang/String;I)I

    move-result p0

    return p0
.end method

.method static synthetic access$1200(Ljava/lang/String;I)V
    .registers 2

    .line 25
    invoke-static {p0, p1}, Lcom/jzs/brawl/JZSModMenu;->setIntPref(Ljava/lang/String;I)V

    return-void
.end method

.method static synthetic access$1300()Landroid/widget/FrameLayout;
    .registers 1

    .line 25
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    return-object v0
.end method

.method static synthetic access$1400()Landroid/view/View;
    .registers 1

    .line 25
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    return-object v0
.end method

.method static synthetic access$1500()Landroid/widget/FrameLayout;
    .registers 1

    .line 25
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    return-object v0
.end method

.method static synthetic access$1600()Landroid/widget/TextView;
    .registers 1

    .line 25
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    return-object v0
.end method

.method static synthetic access$1700()F
    .registers 1

    .line 25
    sget v0, Lcom/jzs/brawl/JZSModMenu;->sHue:F

    return v0
.end method

.method static synthetic access$1702(F)F
    .registers 1

    .line 25
    sput p0, Lcom/jzs/brawl/JZSModMenu;->sHue:F

    return p0
.end method

.method static synthetic access$1800()Landroid/os/Handler;
    .registers 1

    .line 25
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sHandler:Landroid/os/Handler;

    return-object v0
.end method

.method static synthetic access$200(Landroid/content/Context;I)V
    .registers 2

    .line 25
    invoke-static {p0, p1}, Lcom/jzs/brawl/JZSModMenu;->vibrate(Landroid/content/Context;I)V

    return-void
.end method

.method static synthetic access$302(Ljava/lang/String;)Ljava/lang/String;
    .registers 1

    .line 25
    sput-object p0, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    return-object p0
.end method

.method static synthetic access$402(Ljava/lang/String;)Ljava/lang/String;
    .registers 1

    .line 25
    sput-object p0, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    return-object p0
.end method

.method static synthetic access$500()V
    .registers 0

    .line 25
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshContent()V

    return-void
.end method

.method static synthetic access$600()V
    .registers 0

    .line 25
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshSidebar()V

    return-void
.end method

.method static synthetic access$700()V
    .registers 0

    .line 25
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshSubTabs()V

    return-void
.end method

.method static synthetic access$800(Ljava/lang/String;)Z
    .registers 1

    .line 25
    invoke-static {p0}, Lcom/jzs/brawl/JZSModMenu;->isFeatureOn(Ljava/lang/String;)Z

    move-result p0

    return p0
.end method

.method static synthetic access$900(Ljava/lang/String;Z)V
    .registers 2

    .line 25
    invoke-static {p0, p1}, Lcom/jzs/brawl/JZSModMenu;->setFeature(Ljava/lang/String;Z)V

    return-void
.end method

.method private static addDivider(Landroid/content/Context;Landroid/widget/LinearLayout;FI)V
    .registers 6

    .line 768
    new-instance v0, Landroid/view/View;

    invoke-direct {v0, p0}, Landroid/view/View;-><init>(Landroid/content/Context;)V

    .line 769
    new-instance p0, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {p0}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 770
    sget-object v1, Landroid/graphics/drawable/GradientDrawable$Orientation;->LEFT_RIGHT:Landroid/graphics/drawable/GradientDrawable$Orientation;

    invoke-virtual {p0, v1}, Landroid/graphics/drawable/GradientDrawable;->setOrientation(Landroid/graphics/drawable/GradientDrawable$Orientation;)V

    .line 771
    const/4 v1, 0x0

    filled-new-array {v1, p3, v1}, [I

    move-result-object p3

    invoke-virtual {p0, p3}, Landroid/graphics/drawable/GradientDrawable;->setColors([I)V

    .line 772
    invoke-virtual {v0, p0}, Landroid/view/View;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 773
    new-instance p0, Landroid/widget/LinearLayout$LayoutParams;

    const/4 p3, 0x1

    invoke-static {p2, p3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p2

    invoke-static {p3, p2}, Ljava/lang/Math;->max(II)I

    move-result p2

    const/4 p3, -0x1

    invoke-direct {p0, p3, p2}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 774
    invoke-virtual {p1, v0, p0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 775
    return-void
.end method

.method private static addHeaderBtn(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;I)V
    .registers 7

    .line 250
    new-instance v0, Landroid/widget/TextView;

    invoke-direct {v0, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 251
    invoke-virtual {v0, p3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 252
    const/high16 p0, 0x40900000    # 4.5f

    invoke-static {p2, p0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p0

    const/4 v1, 0x2

    invoke-virtual {v0, v1, p0}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 253
    const/16 p0, 0x11

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setGravity(I)V

    .line 254
    const/4 p0, -0x1

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 256
    const/16 p0, 0x1c

    invoke-static {p2, p0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p0

    .line 257
    const/high16 v1, 0x40c00000    # 6.0f

    invoke-static {p2, v1}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v1

    invoke-static {p4, v1}, Lcom/jzs/brawl/JZSModMenu;->make3dButton(IF)Landroid/graphics/drawable/GradientDrawable;

    move-result-object p4

    .line 258
    invoke-virtual {v0, p4}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 259
    const/4 p4, 0x0

    invoke-virtual {v0, p4, p4, p4, p4}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 261
    new-instance p4, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {p4, p0, p0}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 262
    const/4 p0, 0x5

    invoke-static {p2, p0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p0

    iput p0, p4, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 263
    invoke-virtual {p1, v0, p4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 265
    const-string p0, "\u2716"

    invoke-virtual {p3, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p0

    .line 266
    new-instance p1, Lcom/jzs/brawl/JZSModMenu$3;

    invoke-direct {p1, p0}, Lcom/jzs/brawl/JZSModMenu$3;-><init>(Z)V

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 272
    return-void
.end method

.method private static addSidebarTab(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;II)V
    .registers 21

    .line 299
    move-object v0, p0

    move/from16 v1, p2

    move-object/from16 v2, p3

    move/from16 v3, p4

    new-instance v4, Landroid/widget/LinearLayout;

    invoke-direct {v4, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 300
    const/4 v5, 0x1

    invoke-virtual {v4, v5}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 301
    const/16 v6, 0x11

    invoke-virtual {v4, v6}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 302
    const/4 v7, 0x6

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v8

    const/16 v9, 0xc

    invoke-static {v1, v9}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v10

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v11

    invoke-static {v1, v9}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v9

    invoke-virtual {v4, v8, v10, v11, v9}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 304
    sget-object v8, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    invoke-virtual {v2, v8}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v8

    .line 305
    new-instance v9, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v9}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 306
    const/4 v10, 0x0

    if-eqz v8, :cond_3d

    const v11, -0xe5e5c0

    goto :goto_3e

    :cond_3d
    move v11, v10

    :goto_3e
    invoke-virtual {v9, v11}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 307
    const/high16 v11, 0x41000000    # 8.0f

    invoke-static {v1, v11}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v12

    invoke-virtual {v9, v12}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 308
    invoke-virtual {v4, v9}, Landroid/widget/LinearLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 311
    new-instance v9, Landroid/view/View;

    invoke-direct {v9, p0}, Landroid/view/View;-><init>(Landroid/content/Context;)V

    .line 312
    new-instance v12, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v12}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 313
    invoke-virtual {v12, v3}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 314
    const/high16 v13, 0x40000000    # 2.0f

    invoke-static {v1, v13}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v13

    invoke-virtual {v12, v13}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 315
    invoke-virtual {v9, v12}, Landroid/view/View;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 316
    new-instance v12, Landroid/widget/LinearLayout$LayoutParams;

    const/16 v13, 0x18

    invoke-static {v1, v13}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v13

    const/4 v14, 0x3

    invoke-static {v1, v14}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v14

    invoke-direct {v12, v13, v14}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 317
    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    iput v7, v12, Landroid/widget/LinearLayout$LayoutParams;->bottomMargin:I

    .line 318
    invoke-virtual {v4, v9, v12}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 321
    new-instance v7, Landroid/widget/TextView;

    invoke-direct {v7, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 322
    invoke-virtual {v2, v10, v5}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v7, v9}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 323
    const/high16 v9, 0x40a00000    # 5.0f

    invoke-static {v1, v9}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v9

    const/4 v10, 0x2

    invoke-virtual {v7, v10, v9}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 324
    const v9, -0x84786c

    const/4 v12, -0x1

    if-eqz v8, :cond_9d

    move v13, v12

    goto :goto_9e

    :cond_9d
    move v13, v9

    :goto_9e
    invoke-virtual {v7, v13}, Landroid/widget/TextView;->setTextColor(I)V

    .line 325
    const-string v13, "sans-serif-medium"

    invoke-static {v13, v5}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v13

    invoke-virtual {v7, v13}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 326
    invoke-virtual {v7, v6}, Landroid/widget/TextView;->setGravity(I)V

    .line 327
    invoke-virtual {v4, v7}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 330
    const/high16 v7, 0x40400000    # 3.0f

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v7

    if-eqz v8, :cond_b9

    move v9, v12

    :cond_b9
    invoke-static {p0, v2, v7, v9}, Lcom/jzs/brawl/JZSModMenu;->makeLabel(Landroid/content/Context;Ljava/lang/String;FI)Landroid/widget/TextView;

    move-result-object v7

    .line 331
    invoke-virtual {v7, v6}, Landroid/widget/TextView;->setGravity(I)V

    .line 332
    new-instance v8, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v9, -0x2

    invoke-direct {v8, v9, v9}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 333
    invoke-static {v1, v10}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v13

    iput v13, v8, Landroid/widget/LinearLayout$LayoutParams;->topMargin:I

    .line 334
    invoke-virtual {v4, v7, v8}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 337
    const/4 v7, 0x4

    if-lez p5, :cond_122

    .line 338
    new-instance v8, Landroid/widget/TextView;

    invoke-direct {v8, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 339
    invoke-static/range {p5 .. p5}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v8, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 340
    const v0, 0x40266666    # 2.6f

    invoke-static {v1, v0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v0

    invoke-virtual {v8, v10, v0}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 341
    invoke-virtual {v8, v12}, Landroid/widget/TextView;->setTextColor(I)V

    .line 342
    invoke-virtual {v8, v6}, Landroid/widget/TextView;->setGravity(I)V

    .line 343
    const/4 v0, 0x5

    invoke-static {v1, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v10

    invoke-static {v1, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v0

    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-virtual {v8, v6, v10, v0, v5}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 344
    new-instance v0, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v0}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 345
    invoke-virtual {v0, v3}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 346
    invoke-static {v1, v11}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v3

    invoke-virtual {v0, v3}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 347
    invoke-virtual {v8, v0}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 348
    new-instance v0, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v0, v9, v9}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 349
    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    iput v3, v0, Landroid/widget/LinearLayout$LayoutParams;->topMargin:I

    .line 350
    invoke-virtual {v4, v8, v0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 353
    :cond_122
    new-instance v0, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v0, v12, v9}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 354
    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v1

    iput v1, v0, Landroid/widget/LinearLayout$LayoutParams;->bottomMargin:I

    .line 355
    move-object/from16 v1, p1

    invoke-virtual {v1, v4, v0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 357
    new-instance v0, Lcom/jzs/brawl/JZSModMenu$4;

    invoke-direct {v0, v2}, Lcom/jzs/brawl/JZSModMenu$4;-><init>(Ljava/lang/String;)V

    invoke-virtual {v4, v0}, Landroid/widget/LinearLayout;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 367
    return-void
.end method

.method public static attach(Landroid/app/Activity;)V
    .registers 2

    .line 107
    sget-boolean v0, Lcom/jzs/brawl/JZSModMenu;->sAttached:Z

    if-eqz v0, :cond_9

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-ne v0, p0, :cond_9

    return-void

    .line 108
    :cond_9
    const/4 v0, 0x1

    sput-boolean v0, Lcom/jzs/brawl/JZSModMenu;->sAttached:Z

    .line 109
    sput-object p0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    .line 110
    new-instance v0, Lcom/jzs/brawl/JZSModMenu$1;

    invoke-direct {v0, p0}, Lcom/jzs/brawl/JZSModMenu$1;-><init>(Landroid/app/Activity;)V

    invoke-virtual {p0, v0}, Landroid/app/Activity;->runOnUiThread(Ljava/lang/Runnable;)V

    .line 117
    return-void
.end method

.method private static build(Landroid/app/Activity;)V
    .registers 11

    .line 121
    invoke-virtual {p0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v0

    iget v0, v0, Landroid/util/DisplayMetrics;->density:F

    .line 122
    invoke-virtual {p0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v1

    iget v1, v1, Landroid/util/DisplayMetrics;->widthPixels:I

    .line 123
    invoke-virtual {p0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v2

    iget v2, v2, Landroid/util/DisplayMetrics;->heightPixels:I

    .line 125
    new-instance v3, Landroid/widget/FrameLayout;

    invoke-direct {v3, p0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    sput-object v3, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    .line 126
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    new-instance v4, Landroid/view/ViewGroup$LayoutParams;

    const/4 v5, -0x1

    invoke-direct {v4, v5, v5}, Landroid/view/ViewGroup$LayoutParams;-><init>(II)V

    invoke-virtual {v3, v4}, Landroid/widget/FrameLayout;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 129
    new-instance v3, Landroid/view/View;

    invoke-direct {v3, p0}, Landroid/view/View;-><init>(Landroid/content/Context;)V

    sput-object v3, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    .line 130
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    const/high16 v4, -0x68000000

    invoke-virtual {v3, v4}, Landroid/view/View;->setBackgroundColor(I)V

    .line 131
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    const/16 v4, 0x8

    invoke-virtual {v3, v4}, Landroid/view/View;->setVisibility(I)V

    .line 132
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    const/4 v6, 0x1

    invoke-virtual {v3, v6}, Landroid/view/View;->setClickable(Z)V

    .line 133
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    new-instance v7, Lcom/jzs/brawl/JZSModMenu$2;

    invoke-direct {v7}, Lcom/jzs/brawl/JZSModMenu$2;-><init>()V

    invoke-virtual {v3, v7}, Landroid/view/View;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 136
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    sget-object v7, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    new-instance v8, Landroid/widget/FrameLayout$LayoutParams;

    invoke-direct {v8, v5, v5}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v3, v7, v8}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 139
    new-instance v3, Landroid/widget/FrameLayout;

    invoke-direct {v3, p0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    sput-object v3, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    .line 140
    new-instance v3, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v3}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 141
    sget-object v7, Landroid/graphics/drawable/GradientDrawable$Orientation;->TOP_BOTTOM:Landroid/graphics/drawable/GradientDrawable$Orientation;

    invoke-virtual {v3, v7}, Landroid/graphics/drawable/GradientDrawable;->setOrientation(Landroid/graphics/drawable/GradientDrawable$Orientation;)V

    .line 142
    const v7, -0xfeeeed8

    const v8, -0xff5f5e6

    const v9, -0xfe5e5d2

    filled-new-array {v9, v7, v8}, [I

    move-result-object v7

    invoke-virtual {v3, v7}, Landroid/graphics/drawable/GradientDrawable;->setColors([I)V

    .line 143
    const/16 v7, 0xc

    invoke-static {v0, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    int-to-float v7, v7

    invoke-virtual {v3, v7}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 144
    invoke-static {v0, v6}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    const v8, 0x30ffffff

    invoke-virtual {v3, v7, v8}, Landroid/graphics/drawable/GradientDrawable;->setStroke(II)V

    .line 145
    sget-object v7, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v7, v3}, Landroid/widget/FrameLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 146
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-static {v0, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    int-to-float v7, v7

    invoke-virtual {v3, v7}, Landroid/widget/FrameLayout;->setElevation(F)V

    .line 147
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v3, v6}, Landroid/widget/FrameLayout;->setClickable(Z)V

    .line 148
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v3, v4}, Landroid/widget/FrameLayout;->setVisibility(I)V

    .line 150
    new-instance v3, Landroid/widget/FrameLayout$LayoutParams;

    int-to-float v1, v1

    const v4, 0x3f51eb85    # 0.82f

    mul-float/2addr v1, v4

    float-to-int v1, v1

    int-to-float v2, v2

    const v4, 0x3f59999a    # 0.85f

    mul-float/2addr v2, v4

    float-to-int v2, v2

    invoke-direct {v3, v1, v2}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V

    .line 152
    const/16 v1, 0x11

    iput v1, v3, Landroid/widget/FrameLayout$LayoutParams;->gravity:I

    .line 153
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v1, v2, v3}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 155
    new-instance v1, Landroid/widget/LinearLayout;

    invoke-direct {v1, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 156
    invoke-virtual {v1, v6}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 157
    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    new-instance v3, Landroid/widget/FrameLayout$LayoutParams;

    invoke-direct {v3, v5, v5}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v2, v1, v3}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 160
    invoke-static {p0, v1, v0}, Lcom/jzs/brawl/JZSModMenu;->buildHeader(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V

    .line 163
    const v2, -0x16baa0

    invoke-static {p0, v1, v0, v2}, Lcom/jzs/brawl/JZSModMenu;->addDivider(Landroid/content/Context;Landroid/widget/LinearLayout;FI)V

    .line 166
    new-instance v2, Landroid/widget/LinearLayout;

    invoke-direct {v2, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 167
    const/4 v3, 0x0

    invoke-virtual {v2, v3}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 168
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    const/high16 v7, 0x3f800000    # 1.0f

    invoke-direct {v4, v5, v3, v7}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 169
    invoke-virtual {v1, v2, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 172
    invoke-static {p0, v2, v0}, Lcom/jzs/brawl/JZSModMenu;->buildSidebar(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V

    .line 175
    new-instance v1, Landroid/widget/LinearLayout;

    invoke-direct {v1, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 176
    invoke-virtual {v1, v6}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 177
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v4, v3, v5, v7}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 178
    invoke-virtual {v2, v1, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 180
    invoke-static {p0, v1, v0}, Lcom/jzs/brawl/JZSModMenu;->buildSubTabs(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V

    .line 181
    invoke-static {p0, v1, v0}, Lcom/jzs/brawl/JZSModMenu;->buildBody(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V

    .line 184
    invoke-static {p0, v0}, Lcom/jzs/brawl/JZSModMenu;->buildFab(Landroid/app/Activity;F)V

    .line 187
    const v0, 0x1020002

    invoke-virtual {p0, v0}, Landroid/app/Activity;->findViewById(I)Landroid/view/View;

    move-result-object p0

    check-cast p0, Landroid/view/ViewGroup;

    .line 188
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    invoke-virtual {p0, v0}, Landroid/view/ViewGroup;->addView(Landroid/view/View;)V

    .line 190
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshContent()V

    .line 191
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->startRainbow()V

    .line 192
    return-void
.end method

.method private static buildBody(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V
    .registers 10

    .line 443
    new-instance v0, Landroid/widget/ScrollView;

    invoke-direct {v0, p0}, Landroid/widget/ScrollView;-><init>(Landroid/content/Context;)V

    .line 444
    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/widget/ScrollView;->setFillViewport(Z)V

    .line 445
    const/4 v2, 0x0

    invoke-virtual {v0, v2}, Landroid/widget/ScrollView;->setVerticalScrollBarEnabled(Z)V

    .line 446
    const/16 v3, 0x8

    invoke-static {p2, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    const/4 v5, 0x4

    invoke-static {p2, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {p2, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    invoke-static {p2, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p2

    invoke-virtual {v0, v4, v5, v6, p2}, Landroid/widget/ScrollView;->setPadding(IIII)V

    .line 448
    new-instance p2, Landroid/widget/LinearLayout;

    invoke-direct {p2, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    sput-object p2, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    .line 449
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    invoke-virtual {p0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 450
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v1, -0x2

    const/4 v3, -0x1

    invoke-direct {p2, v3, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v0, p0, p2}, Landroid/widget/ScrollView;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 452
    new-instance p0, Landroid/widget/LinearLayout$LayoutParams;

    const/high16 p2, 0x3f800000    # 1.0f

    invoke-direct {p0, v3, v2, p2}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 453
    invoke-virtual {p1, v0, p0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 454
    return-void
.end method

.method private static buildFab(Landroid/app/Activity;F)V
    .registers 7

    .line 646
    new-instance v0, Landroid/widget/FrameLayout;

    invoke-direct {v0, p0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    .line 647
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/widget/FrameLayout;->setClickable(Z)V

    .line 649
    const/16 v0, 0x30

    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v0

    .line 650
    const/high16 v2, 0x41c00000    # 24.0f

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v2

    const v3, -0x16baa0

    invoke-static {v3, v2}, Lcom/jzs/brawl/JZSModMenu;->make3dButton(IF)Landroid/graphics/drawable/GradientDrawable;

    move-result-object v2

    .line 651
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    invoke-virtual {v3, v2}, Landroid/widget/FrameLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 652
    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    const/high16 v3, 0x40c00000    # 6.0f

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v3

    invoke-virtual {v2, v3}, Landroid/widget/FrameLayout;->setElevation(F)V

    .line 654
    new-instance v2, Landroid/widget/TextView;

    invoke-direct {v2, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 655
    const-string p0, "JZS"

    invoke-virtual {v2, p0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 656
    const/4 p0, -0x1

    invoke-virtual {v2, p0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 657
    const v3, 0x40733333    # 3.8f

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v3

    const/4 v4, 0x2

    invoke-virtual {v2, v4, v3}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 658
    const-string v3, "sans-serif-black"

    invoke-static {v3, v1}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v1

    invoke-virtual {v2, v1}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 659
    const/16 v1, 0x11

    invoke-virtual {v2, v1}, Landroid/widget/TextView;->setGravity(I)V

    .line 660
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    new-instance v3, Landroid/widget/FrameLayout$LayoutParams;

    invoke-direct {v3, p0, p0}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v1, v2, v3}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 662
    new-instance p0, Landroid/widget/FrameLayout$LayoutParams;

    invoke-direct {p0, v0, v0}, Landroid/widget/FrameLayout$LayoutParams;-><init>(II)V

    .line 663
    const v0, 0x800053

    iput v0, p0, Landroid/widget/FrameLayout$LayoutParams;->gravity:I

    .line 664
    const/16 v0, 0xc

    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v1

    iput v1, p0, Landroid/widget/FrameLayout$LayoutParams;->leftMargin:I

    .line 665
    invoke-static {p1, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p1

    iput p1, p0, Landroid/widget/FrameLayout$LayoutParams;->bottomMargin:I

    .line 666
    sget-object p1, Lcom/jzs/brawl/JZSModMenu;->sRootFrame:Landroid/widget/FrameLayout;

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    invoke-virtual {p1, v0, p0}, Landroid/widget/FrameLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 668
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    new-instance p1, Lcom/jzs/brawl/JZSModMenu$9;

    invoke-direct {p1}, Lcom/jzs/brawl/JZSModMenu$9;-><init>()V

    invoke-virtual {p0, p1}, Landroid/widget/FrameLayout;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 676
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    const/4 p1, 0x0

    invoke-virtual {p0, p1}, Landroid/widget/FrameLayout;->setAlpha(F)V

    .line 677
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    invoke-virtual {p0}, Landroid/widget/FrameLayout;->animate()Landroid/view/ViewPropertyAnimator;

    move-result-object p0

    const/high16 p1, 0x3f800000    # 1.0f

    invoke-virtual {p0, p1}, Landroid/view/ViewPropertyAnimator;->alpha(F)Landroid/view/ViewPropertyAnimator;

    move-result-object p0

    const-wide/16 v0, 0x1f4

    invoke-virtual {p0, v0, v1}, Landroid/view/ViewPropertyAnimator;->setDuration(J)Landroid/view/ViewPropertyAnimator;

    move-result-object p0

    invoke-virtual {p0}, Landroid/view/ViewPropertyAnimator;->start()V

    .line 678
    return-void
.end method

.method private static buildHeader(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V
    .registers 19

    .line 196
    move-object/from16 v0, p0

    move/from16 v1, p2

    new-instance v2, Landroid/widget/LinearLayout;

    invoke-direct {v2, v0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 197
    const/4 v3, 0x0

    invoke-virtual {v2, v3}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 198
    const/16 v4, 0x10

    invoke-virtual {v2, v4}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 199
    const/16 v4, 0xe

    invoke-static {v1, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    const/16 v6, 0xa

    invoke-static {v1, v6}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    invoke-static {v1, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    invoke-static {v1, v6}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    invoke-virtual {v2, v5, v7, v4, v6}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 201
    new-instance v4, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v4}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 202
    const v5, -0xf5f5e6

    invoke-virtual {v4, v5}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 203
    const/16 v5, 0x8

    new-array v6, v5, [F

    .line 204
    const/high16 v7, 0x41400000    # 12.0f

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v8

    aput v8, v6, v3

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v8

    const/4 v9, 0x1

    aput v8, v6, v9

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v8

    const/4 v10, 0x2

    aput v8, v6, v10

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v7

    const/4 v8, 0x3

    aput v7, v6, v8

    const/4 v7, 0x4

    const/4 v11, 0x0

    aput v11, v6, v7

    const/4 v7, 0x5

    aput v11, v6, v7

    const/4 v7, 0x6

    aput v11, v6, v7

    const/4 v12, 0x7

    aput v11, v6, v12

    .line 203
    invoke-virtual {v4, v6}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadii([F)V

    .line 205
    invoke-virtual {v2, v4}, Landroid/widget/LinearLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 208
    new-instance v4, Landroid/widget/TextView;

    invoke-direct {v4, v0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 209
    const-string v6, "JZS"

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 210
    const/4 v6, -0x1

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setTextColor(I)V

    .line 211
    const/high16 v11, 0x40b00000    # 5.5f

    invoke-static {v1, v11}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v12

    invoke-virtual {v4, v10, v12}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 212
    const-string v12, "sans-serif-black"

    invoke-static {v12, v9}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v13

    invoke-virtual {v4, v13}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 213
    const/16 v13, 0x11

    invoke-virtual {v4, v13}, Landroid/widget/TextView;->setGravity(I)V

    .line 214
    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v13

    invoke-static {v1, v8}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v14

    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v15

    invoke-static {v1, v8}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v8

    invoke-virtual {v4, v13, v14, v15, v8}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 215
    new-instance v8, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v8}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 216
    const v13, -0x16baa0

    invoke-virtual {v8, v13}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 217
    const/high16 v14, 0x40c00000    # 6.0f

    invoke-static {v1, v14}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v14

    invoke-virtual {v8, v14}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 218
    invoke-virtual {v4, v8}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 219
    invoke-virtual {v2, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 222
    new-instance v4, Landroid/widget/TextView;

    invoke-direct {v4, v0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    sput-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    .line 223
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    const-string v8, "JZS Brawl"

    invoke-virtual {v4, v8}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 224
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setTextColor(I)V

    .line 225
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    invoke-static {v1, v11}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v6

    invoke-virtual {v4, v10, v6}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 226
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    invoke-static {v12, v9}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v6

    invoke-virtual {v4, v6}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 227
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-virtual {v4, v5, v3, v3, v3}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 228
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v5, -0x2

    const/high16 v6, 0x3f800000    # 1.0f

    invoke-direct {v4, v3, v5, v6}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 229
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sFabText:Landroid/widget/TextView;

    invoke-virtual {v2, v3, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 232
    const v3, 0x404ccccd    # 3.2f

    invoke-static {v1, v3}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v3

    const v4, -0x84786c

    const-string v5, "v5.2"

    invoke-static {v0, v5, v3, v4}, Lcom/jzs/brawl/JZSModMenu;->makeLabel(Landroid/content/Context;Ljava/lang/String;FI)Landroid/widget/TextView;

    move-result-object v3

    .line 233
    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    invoke-static {v1, v10}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {v1, v7}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    invoke-static {v1, v10}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    invoke-virtual {v3, v4, v5, v6, v7}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 234
    new-instance v4, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v4}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 235
    const v5, 0x20ffffff

    invoke-virtual {v4, v5}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 236
    const/high16 v5, 0x40800000    # 4.0f

    invoke-static {v1, v5}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v5

    invoke-virtual {v4, v5}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 237
    invoke-virtual {v3, v4}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 238
    invoke-virtual {v2, v3}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 241
    const-string v3, "\u2699"

    const/16 v4, -0x3ef9

    invoke-static {v0, v2, v1, v3, v4}, Lcom/jzs/brawl/JZSModMenu;->addHeaderBtn(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;I)V

    .line 242
    const-string v3, "\u270e"

    const v4, -0xde690d

    invoke-static {v0, v2, v1, v3, v4}, Lcom/jzs/brawl/JZSModMenu;->addHeaderBtn(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;I)V

    .line 243
    const-string v3, "\u2716"

    invoke-static {v0, v2, v1, v3, v13}, Lcom/jzs/brawl/JZSModMenu;->addHeaderBtn(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;I)V

    .line 245
    move-object/from16 v0, p1

    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 246
    return-void
.end method

.method private static buildSidebar(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V
    .registers 12

    .line 276
    new-instance v0, Landroid/widget/ScrollView;

    invoke-direct {v0, p0}, Landroid/widget/ScrollView;-><init>(Landroid/content/Context;)V

    .line 277
    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/widget/ScrollView;->setFillViewport(Z)V

    .line 278
    const/4 v2, 0x0

    invoke-virtual {v0, v2}, Landroid/widget/ScrollView;->setVerticalScrollBarEnabled(Z)V

    .line 280
    new-instance v3, Landroid/widget/LinearLayout;

    invoke-direct {v3, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    sput-object v3, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    .line 281
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    invoke-virtual {v3, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 282
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    const/4 v3, 0x4

    invoke-static {p2, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    const/16 v5, 0x8

    invoke-static {p2, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    invoke-static {p2, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    invoke-static {p2, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-virtual {v1, v4, v6, v3, v5}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 284
    new-instance v1, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v1}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 285
    const v3, -0xeeeed0

    invoke-virtual {v1, v3}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 286
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    invoke-virtual {v3, v1}, Landroid/widget/LinearLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 288
    nop

    :goto_42
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    array-length v1, v1

    if-ge v2, v1, :cond_61

    .line 289
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    aget-object v6, v1, v2

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->GROUP_COLORS:[I

    aget v7, v1, v2

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    aget-object v1, v1, v2

    invoke-static {v1}, Lcom/jzs/brawl/JZSModMenu;->getGroupCount(Ljava/lang/String;)I

    move-result v8

    move-object v3, p0

    move v5, p2

    invoke-static/range {v3 .. v8}, Lcom/jzs/brawl/JZSModMenu;->addSidebarTab(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;II)V

    .line 288
    add-int/lit8 v2, v2, 0x1

    goto :goto_42

    .line 292
    :cond_61
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    new-instance v1, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v2, -0x2

    const/4 v3, -0x1

    invoke-direct {v1, v3, v2}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v0, p0, v1}, Landroid/widget/ScrollView;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 293
    new-instance p0, Landroid/widget/LinearLayout$LayoutParams;

    const/16 v1, 0x50

    invoke-static {p2, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p2

    invoke-direct {p0, p2, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 294
    invoke-virtual {p1, v0, p0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 295
    return-void
.end method

.method private static buildSubTabs(Landroid/app/Activity;Landroid/widget/LinearLayout;F)V
    .registers 9

    .line 371
    new-instance v0, Landroid/widget/HorizontalScrollView;

    invoke-direct {v0, p0}, Landroid/widget/HorizontalScrollView;-><init>(Landroid/content/Context;)V

    .line 372
    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/HorizontalScrollView;->setHorizontalScrollBarEnabled(Z)V

    .line 373
    const/16 v2, 0x8

    invoke-static {p2, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    const/4 v4, 0x6

    invoke-static {p2, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {p2, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v2

    invoke-static {p2, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p2

    invoke-virtual {v0, v3, v5, v2, p2}, Landroid/widget/HorizontalScrollView;->setPadding(IIII)V

    .line 375
    new-instance p2, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {p2}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 376
    const v2, -0xeeeed8

    invoke-virtual {p2, v2}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 377
    invoke-virtual {v0, p2}, Landroid/widget/HorizontalScrollView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 379
    new-instance p2, Landroid/widget/LinearLayout;

    invoke-direct {p2, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    sput-object p2, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    .line 380
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    invoke-virtual {p0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 381
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    const/16 p2, 0x10

    invoke-virtual {p0, p2}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 382
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v1, -0x2

    invoke-direct {p2, v1, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v0, p0, p2}, Landroid/widget/HorizontalScrollView;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 384
    new-instance p0, Landroid/widget/LinearLayout$LayoutParams;

    const/4 p2, -0x1

    invoke-direct {p0, p2, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, v0, p0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 385
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshSubTabs()V

    .line 386
    return-void
.end method

.method private static dp(FI)I
    .registers 2

    .line 882
    int-to-float p1, p1

    mul-float/2addr p1, p0

    const/high16 p0, 0x3f000000    # 0.5f

    add-float/2addr p1, p0

    float-to-int p0, p1

    return p0
.end method

.method private static dpf(FF)F
    .registers 2

    .line 886
    mul-float/2addr p1, p0

    return p1
.end method

.method private static getGroupCount(Ljava/lang/String;)I
    .registers 7

    .line 726
    nop

    .line 727
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->FEATURES:[[Ljava/lang/String;

    array-length v1, v0

    const/4 v2, 0x0

    move v3, v2

    :goto_6
    if-ge v2, v1, :cond_18

    aget-object v4, v0, v2

    .line 728
    const/4 v5, 0x2

    aget-object v4, v4, v5

    invoke-virtual {v4, p0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_15

    add-int/lit8 v3, v3, 0x1

    .line 727
    :cond_15
    add-int/lit8 v2, v2, 0x1

    goto :goto_6

    .line 730
    :cond_18
    return v3
.end method

.method private static getIntPref(Ljava/lang/String;I)I
    .registers 6

    .line 856
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->loadSettings()Ljava/util/ArrayList;

    move-result-object v0

    .line 857
    invoke-virtual {v0}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_8
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_43

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 858
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    const-string v3, "="

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v2

    if-eqz v2, :cond_42

    .line 859
    :try_start_2d
    invoke-virtual {p0}, Ljava/lang/String;->length()I

    move-result p0

    add-int/lit8 p0, p0, 0x1

    invoke-virtual {v1, p0}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object p0

    invoke-static {p0}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result p0
    :try_end_3f
    .catch Ljava/lang/Exception; {:try_start_2d .. :try_end_3f} :catch_40

    return p0

    .line 860
    :catch_40
    move-exception p0

    return p1

    .line 862
    :cond_42
    goto :goto_8

    .line 863
    :cond_43
    return p1
.end method

.method private static isFeatureOn(Ljava/lang/String;)Z
    .registers 6

    .line 833
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->loadSettings()Ljava/util/ArrayList;

    move-result-object v0

    .line 834
    invoke-virtual {v0}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_8
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    const/4 v2, 0x0

    if-eqz v1, :cond_4b

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    .line 835
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, "=1"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_30

    const/4 p0, 0x1

    return p0

    .line 836
    :cond_30
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, "=0"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_4a

    return v2

    .line 837
    :cond_4a
    goto :goto_8

    .line 838
    :cond_4b
    return v2
.end method

.method private static loadSettings()Ljava/util/ArrayList;
    .registers 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/ArrayList<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation

    .line 796
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    if-eqz v0, :cond_7

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    return-object v0

    .line 797
    :cond_7
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    .line 798
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-nez v0, :cond_15

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    return-object v0

    .line 800
    :cond_15
    :try_start_15
    new-instance v0, Ljava/io/File;

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v1}, Landroid/app/Activity;->getFilesDir()Ljava/io/File;

    move-result-object v1

    const-string v2, "jzs_mod_settings.txt"

    invoke-direct {v0, v1, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 801
    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v1

    if-nez v1, :cond_2b

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    return-object v0

    .line 802
    :cond_2b
    new-instance v1, Ljava/io/FileInputStream;

    invoke-direct {v1, v0}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V

    .line 803
    new-instance v0, Ljava/io/ByteArrayOutputStream;

    invoke-direct {v0}, Ljava/io/ByteArrayOutputStream;-><init>()V

    .line 804
    const/16 v2, 0x1000

    new-array v2, v2, [B

    .line 806
    :goto_39
    invoke-virtual {v1, v2}, Ljava/io/FileInputStream;->read([B)I

    move-result v3

    const/4 v4, 0x0

    if-lez v3, :cond_44

    invoke-virtual {v0, v2, v4, v3}, Ljava/io/ByteArrayOutputStream;->write([BII)V

    goto :goto_39

    .line 807
    :cond_44
    invoke-virtual {v1}, Ljava/io/FileInputStream;->close()V

    .line 808
    const-string v1, "UTF-8"

    invoke-virtual {v0, v1}, Ljava/io/ByteArrayOutputStream;->toString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    const-string v1, "\n"

    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 809
    array-length v1, v0

    :goto_54
    if-ge v4, v1, :cond_6b

    aget-object v2, v0, v4

    .line 810
    invoke-virtual {v2}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object v2

    .line 811
    invoke-virtual {v2}, Ljava/lang/String;->length()I

    move-result v3

    if-lez v3, :cond_67

    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    invoke-virtual {v3, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    :try_end_67
    .catch Ljava/lang/Exception; {:try_start_15 .. :try_end_67} :catch_6a

    .line 809
    :cond_67
    add-int/lit8 v4, v4, 0x1

    goto :goto_54

    .line 813
    :catch_6a
    move-exception v0

    :cond_6b
    nop

    .line 814
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    return-object v0
.end method

.method private static make3dButton(IF)Landroid/graphics/drawable/GradientDrawable;
    .registers 10

    .line 735
    new-instance v0, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v0}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 736
    invoke-static {p0}, Landroid/graphics/Color;->red(I)I

    move-result v1

    invoke-static {p0}, Landroid/graphics/Color;->green(I)I

    move-result v2

    invoke-static {p0}, Landroid/graphics/Color;->blue(I)I

    move-result v3

    .line 737
    add-int/lit8 v4, v1, 0x1e

    const/16 v5, 0xff

    invoke-static {v5, v4}, Ljava/lang/Math;->min(II)I

    move-result v4

    add-int/lit8 v6, v2, 0x1e

    invoke-static {v5, v6}, Ljava/lang/Math;->min(II)I

    move-result v6

    add-int/lit8 v7, v3, 0x1e

    invoke-static {v5, v7}, Ljava/lang/Math;->min(II)I

    move-result v7

    invoke-static {v5, v4, v6, v7}, Landroid/graphics/Color;->argb(IIII)I

    move-result v4

    .line 738
    add-int/lit8 v1, v1, -0x28

    const/4 v6, 0x0

    invoke-static {v6, v1}, Ljava/lang/Math;->max(II)I

    move-result v1

    add-int/lit8 v2, v2, -0x28

    invoke-static {v6, v2}, Ljava/lang/Math;->max(II)I

    move-result v2

    add-int/lit8 v3, v3, -0x28

    invoke-static {v6, v3}, Ljava/lang/Math;->max(II)I

    move-result v3

    invoke-static {v5, v1, v2, v3}, Landroid/graphics/Color;->argb(IIII)I

    move-result v1

    .line 739
    sget-object v2, Landroid/graphics/drawable/GradientDrawable$Orientation;->TOP_BOTTOM:Landroid/graphics/drawable/GradientDrawable$Orientation;

    invoke-virtual {v0, v2}, Landroid/graphics/drawable/GradientDrawable;->setOrientation(Landroid/graphics/drawable/GradientDrawable$Orientation;)V

    .line 740
    filled-new-array {v4, p0, v1}, [I

    move-result-object p0

    invoke-virtual {v0, p0}, Landroid/graphics/drawable/GradientDrawable;->setColors([I)V

    .line 741
    invoke-virtual {v0, p1}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 742
    const/4 p0, 0x1

    const p1, 0x20ffffff

    invoke-virtual {v0, p0, p1}, Landroid/graphics/drawable/GradientDrawable;->setStroke(II)V

    .line 743
    return-object v0
.end method

.method private static make3dCard(F)Landroid/graphics/drawable/GradientDrawable;
    .registers 5

    .line 747
    new-instance v0, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v0}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 748
    sget-object v1, Landroid/graphics/drawable/GradientDrawable$Orientation;->TOP_BOTTOM:Landroid/graphics/drawable/GradientDrawable$Orientation;

    invoke-virtual {v0, v1}, Landroid/graphics/drawable/GradientDrawable;->setOrientation(Landroid/graphics/drawable/GradientDrawable$Orientation;)V

    .line 749
    const v1, -0xe9dec2

    const v2, -0xf1ead0

    const v3, -0xe1d8ba

    filled-new-array {v3, v1, v2}, [I

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/graphics/drawable/GradientDrawable;->setColors([I)V

    .line 750
    const/high16 v1, 0x41000000    # 8.0f

    invoke-static {p0, v1}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v1

    invoke-virtual {v0, v1}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 751
    const/4 v1, 0x1

    invoke-static {p0, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p0

    const v1, 0x18ffffff

    invoke-virtual {v0, p0, v1}, Landroid/graphics/drawable/GradientDrawable;->setStroke(II)V

    .line 752
    return-object v0
.end method

.method private static make3dMiniButton(Landroid/content/Context;FLjava/lang/String;I)Landroid/widget/TextView;
    .registers 5

    .line 756
    new-instance v0, Landroid/widget/TextView;

    invoke-direct {v0, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 757
    invoke-virtual {v0, p2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 758
    const p0, 0x40866666    # 4.2f

    invoke-static {p1, p0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p0

    const/4 p2, 0x2

    invoke-virtual {v0, p2, p0}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 759
    const/4 p0, -0x1

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 760
    const/16 p0, 0x11

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setGravity(I)V

    .line 761
    const-string p0, "sans-serif-black"

    const/4 p2, 0x1

    invoke-static {p0, p2}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object p0

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 762
    const/high16 p0, 0x40c00000    # 6.0f

    invoke-static {p1, p0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p0

    invoke-static {p3, p0}, Lcom/jzs/brawl/JZSModMenu;->make3dButton(IF)Landroid/graphics/drawable/GradientDrawable;

    move-result-object p0

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 763
    return-object v0
.end method

.method private static makeFeatureCard(Landroid/content/Context;FLjava/lang/String;Ljava/lang/String;)Landroid/view/View;
    .registers 10

    .line 510
    new-instance v0, Landroid/widget/LinearLayout;

    invoke-direct {v0, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 511
    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 512
    const/16 v2, 0x10

    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 513
    const/16 v2, 0xa

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    const/16 v5, 0x8

    invoke-static {p1, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v2

    invoke-virtual {v0, v3, v4, v5, v2}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 515
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->make3dCard(F)Landroid/graphics/drawable/GradientDrawable;

    move-result-object v2

    .line 516
    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 519
    new-instance v2, Landroid/widget/TextView;

    invoke-direct {v2, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 520
    invoke-virtual {v2, p2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 521
    const p2, -0x1b1b1c

    invoke-virtual {v2, p2}, Landroid/widget/TextView;->setTextColor(I)V

    .line 522
    const p2, 0x40666666    # 3.6f

    invoke-static {p1, p2}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p2

    const/4 v3, 0x2

    invoke-virtual {v2, v3, p2}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 523
    const-string p2, "sans-serif-medium"

    invoke-static {p2, v1}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object p2

    invoke-virtual {v2, p2}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 524
    const/4 p2, 0x1

    invoke-virtual {v2, p2}, Landroid/widget/TextView;->setSingleLine(Z)V

    .line 525
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/high16 v4, 0x3f800000    # 1.0f

    const/4 v5, -0x2

    invoke-direct {p2, v1, v5, v4}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 526
    invoke-virtual {v0, v2, p2}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 529
    new-instance p2, Landroid/widget/TextView;

    invoke-direct {p2, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 530
    invoke-static {p3}, Lcom/jzs/brawl/JZSModMenu;->isFeatureOn(Ljava/lang/String;)Z

    move-result v1

    .line 531
    invoke-static {p2, v1, p1}, Lcom/jzs/brawl/JZSModMenu;->updateBadge(Landroid/widget/TextView;ZF)V

    .line 533
    new-instance v1, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v1, v5, v5}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 534
    const/4 v2, 0x4

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    iput v4, v1, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 535
    invoke-virtual {v0, p2, v1}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 538
    new-instance v1, Landroid/widget/TextView;

    invoke-direct {v1, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 539
    const-string p0, "\u2699"

    invoke-virtual {v1, p0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 540
    const p0, 0x40866666    # 4.2f

    invoke-static {p1, p0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p0

    invoke-virtual {v1, v3, p0}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 541
    const p0, -0x84786c

    invoke-virtual {v1, p0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 542
    const/16 p0, 0x11

    invoke-virtual {v1, p0}, Landroid/widget/TextView;->setGravity(I)V

    .line 543
    new-instance p0, Landroid/widget/LinearLayout$LayoutParams;

    const/16 v3, 0x16

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    invoke-direct {p0, v4, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 544
    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p1

    iput p1, p0, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 545
    invoke-virtual {v0, v1, p0}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 547
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sToggleViews:Ljava/util/ArrayList;

    filled-new-array {p2, p3}, [Ljava/lang/Object;

    move-result-object p1

    invoke-virtual {p0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 549
    new-instance p0, Lcom/jzs/brawl/JZSModMenu$6;

    invoke-direct {p0, p3}, Lcom/jzs/brawl/JZSModMenu$6;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0, p0}, Landroid/widget/LinearLayout;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 558
    return-object v0
.end method

.method private static makeLabel(Landroid/content/Context;Ljava/lang/String;FI)Landroid/widget/TextView;
    .registers 5

    .line 890
    new-instance v0, Landroid/widget/TextView;

    invoke-direct {v0, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 891
    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 892
    const/4 p0, 0x2

    invoke-virtual {v0, p0, p2}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 893
    invoke-virtual {v0, p3}, Landroid/widget/TextView;->setTextColor(I)V

    .line 894
    const-string p0, "sans-serif-medium"

    const/4 p1, 0x0

    invoke-static {p0, p1}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object p0

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 895
    return-object v0
.end method

.method private static makeSliderCard(Landroid/content/Context;F[Ljava/lang/String;)Landroid/view/View;
    .registers 11

    .line 577
    new-instance v0, Landroid/widget/LinearLayout;

    invoke-direct {v0, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 578
    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 579
    const/16 v2, 0x10

    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 580
    const/16 v2, 0xa

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    const/16 v4, 0x8

    invoke-static {p1, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v2

    invoke-static {p1, v4}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v4

    invoke-virtual {v0, v3, v5, v2, v4}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 582
    invoke-static {p1}, Lcom/jzs/brawl/JZSModMenu;->make3dCard(F)Landroid/graphics/drawable/GradientDrawable;

    move-result-object v2

    .line 583
    invoke-virtual {v0, v2}, Landroid/widget/LinearLayout;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 586
    aget-object v2, p2, v1

    const v3, 0x40666666    # 3.6f

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v3

    const v4, -0x1b1b1c

    invoke-static {p0, v2, v3, v4}, Lcom/jzs/brawl/JZSModMenu;->makeLabel(Landroid/content/Context;Ljava/lang/String;FI)Landroid/widget/TextView;

    move-result-object v2

    .line 587
    const-string v3, "sans-serif-medium"

    invoke-static {v3, v1}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v5

    invoke-virtual {v2, v5}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 588
    new-instance v5, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v6, -0x2

    const/high16 v7, 0x3f800000    # 1.0f

    invoke-direct {v5, v1, v6, v7}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 589
    invoke-virtual {v0, v2, v5}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 592
    const/4 v1, 0x3

    aget-object v1, p2, v1

    invoke-static {v1}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    .line 593
    const/4 v1, 0x4

    aget-object v2, p2, v1

    invoke-static {v2}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    .line 594
    const/4 v2, 0x5

    aget-object v2, p2, v2

    invoke-static {v2}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    .line 595
    const/4 v2, 0x6

    aget-object v2, p2, v2

    invoke-static {v2}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v2

    .line 597
    new-instance v5, Landroid/widget/TextView;

    invoke-direct {v5, p0}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 598
    const/4 v6, 0x1

    aget-object v7, p2, v6

    invoke-static {v7, v2}, Lcom/jzs/brawl/JZSModMenu;->getIntPref(Ljava/lang/String;I)I

    move-result v2

    .line 599
    invoke-static {v2}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v5, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 600
    const/high16 v2, 0x40800000    # 4.0f

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v2

    const/4 v7, 0x2

    invoke-virtual {v5, v7, v2}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 601
    invoke-virtual {v5, v4}, Landroid/widget/TextView;->setTextColor(I)V

    .line 602
    invoke-static {v3, v6}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v2

    invoke-virtual {v5, v2}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 603
    const/16 v2, 0x11

    invoke-virtual {v5, v2}, Landroid/widget/TextView;->setGravity(I)V

    .line 604
    const/16 v2, 0x28

    invoke-static {p1, v2}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v2

    invoke-virtual {v5, v2}, Landroid/widget/TextView;->setMinWidth(I)V

    .line 606
    const-string v2, "\u2212"

    const v3, -0x16baa0

    invoke-static {p0, p1, v2, v3}, Lcom/jzs/brawl/JZSModMenu;->make3dMiniButton(Landroid/content/Context;FLjava/lang/String;I)Landroid/widget/TextView;

    move-result-object v2

    .line 607
    const-string v3, "+"

    const v4, -0xff339a

    invoke-static {p0, p1, v3, v4}, Lcom/jzs/brawl/JZSModMenu;->make3dMiniButton(Landroid/content/Context;FLjava/lang/String;I)Landroid/widget/TextView;

    move-result-object p0

    .line 609
    new-instance v3, Lcom/jzs/brawl/JZSModMenu$7;

    invoke-direct {v3, p2, v5}, Lcom/jzs/brawl/JZSModMenu$7;-><init>([Ljava/lang/String;Landroid/widget/TextView;)V

    invoke-virtual {v2, v3}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 618
    new-instance v3, Lcom/jzs/brawl/JZSModMenu$8;

    invoke-direct {v3, p2, v5}, Lcom/jzs/brawl/JZSModMenu$8;-><init>([Ljava/lang/String;Landroid/widget/TextView;)V

    invoke-virtual {p0, v3}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 628
    const/16 v3, 0x1a

    invoke-static {p1, v3}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    .line 629
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v4, v3, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 630
    invoke-static {p1, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    iput v6, v4, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 632
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v4, v3, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {v0, v2, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 633
    invoke-virtual {v0, v5}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 635
    new-instance v2, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v2, v3, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 636
    invoke-static {p1, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result p1

    iput p1, v2, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 637
    invoke-virtual {v0, p0, v2}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 639
    sget-object p0, Lcom/jzs/brawl/JZSModMenu;->sSliderViews:Ljava/util/ArrayList;

    filled-new-array {v5, p2}, [Ljava/lang/Object;

    move-result-object p1

    invoke-virtual {p0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 641
    return-object v0
.end method

.method private static refreshContent()V
    .registers 16

    .line 457
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    if-eqz v0, :cond_fb

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-nez v0, :cond_a

    goto/16 :goto_fb

    .line 458
    :cond_a
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    invoke-virtual {v0}, Landroid/widget/LinearLayout;->removeAllViews()V

    .line 459
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sToggleViews:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->clear()V

    .line 460
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSliderViews:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->clear()V

    .line 462
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v0

    iget v0, v0, Landroid/util/DisplayMetrics;->density:F

    .line 463
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    const-string v2, "All"

    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    const/4 v2, 0x1

    const/4 v3, 0x0

    if-nez v1, :cond_3c

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    invoke-virtual {v1}, Ljava/lang/String;->isEmpty()Z

    move-result v1

    if-eqz v1, :cond_3a

    goto :goto_3c

    :cond_3a
    move v1, v3

    goto :goto_3d

    :cond_3c
    :goto_3c
    move v1, v2

    .line 466
    :goto_3d
    nop

    .line 467
    nop

    .line 468
    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->FEATURES:[[Ljava/lang/String;

    array-length v5, v4

    const/4 v6, 0x0

    move v7, v3

    move v8, v7

    :goto_45
    const/4 v9, 0x4

    const/4 v10, -0x1

    const/high16 v11, 0x3f800000    # 1.0f

    const/4 v12, -0x2

    const/4 v13, 0x2

    if-ge v7, v5, :cond_aa

    aget-object v14, v4, v7

    .line 469
    aget-object v15, v14, v13

    sget-object v13, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    invoke-virtual {v15, v13}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_5a

    goto :goto_a7

    .line 470
    :cond_5a
    if-nez v1, :cond_68

    const/4 v13, 0x3

    aget-object v13, v14, v13

    sget-object v15, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    invoke-virtual {v13, v15}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v13

    if-nez v13, :cond_68

    goto :goto_a7

    .line 472
    :cond_68
    rem-int/lit8 v13, v8, 0x2

    if-nez v13, :cond_86

    .line 473
    new-instance v6, Landroid/widget/LinearLayout;

    sget-object v13, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-direct {v6, v13}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 474
    invoke-virtual {v6, v3}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 475
    new-instance v13, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v13, v10, v12}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 476
    invoke-static {v0, v9}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v9

    iput v9, v13, Landroid/widget/LinearLayout$LayoutParams;->bottomMargin:I

    .line 477
    sget-object v9, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    invoke-virtual {v9, v6, v13}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 480
    :cond_86
    sget-object v9, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    aget-object v10, v14, v3

    aget-object v13, v14, v2

    invoke-static {v9, v0, v10, v13}, Lcom/jzs/brawl/JZSModMenu;->makeFeatureCard(Landroid/content/Context;FLjava/lang/String;Ljava/lang/String;)Landroid/view/View;

    move-result-object v9

    .line 481
    new-instance v10, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v10, v3, v12, v11}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 482
    const/4 v11, 0x2

    invoke-static {v0, v11}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v12

    iput v12, v10, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 483
    invoke-static {v0, v11}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v11

    iput v11, v10, Landroid/widget/LinearLayout$LayoutParams;->rightMargin:I

    .line 484
    invoke-virtual {v6, v9, v10}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 485
    add-int/lit8 v8, v8, 0x1

    .line 468
    :goto_a7
    add-int/lit8 v7, v7, 0x1

    goto :goto_45

    .line 488
    :cond_aa
    if-eqz v6, :cond_cb

    const/4 v1, 0x2

    rem-int/2addr v8, v1

    if-ne v8, v2, :cond_cb

    .line 489
    new-instance v2, Landroid/view/View;

    sget-object v4, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-direct {v2, v4}, Landroid/view/View;-><init>(Landroid/content/Context;)V

    .line 490
    new-instance v4, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v4, v3, v3, v11}, Landroid/widget/LinearLayout$LayoutParams;-><init>(IIF)V

    .line 491
    invoke-static {v0, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    iput v5, v4, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 492
    invoke-static {v0, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    iput v5, v4, Landroid/widget/LinearLayout$LayoutParams;->rightMargin:I

    .line 493
    invoke-virtual {v6, v2, v4}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 497
    :cond_cb
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->SLIDERS:[[Ljava/lang/String;

    array-length v2, v1

    :goto_ce
    if-ge v3, v2, :cond_f7

    aget-object v4, v1, v3

    .line 498
    const/4 v5, 0x2

    aget-object v6, v4, v5

    sget-object v7, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    invoke-virtual {v6, v7}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v6

    if-nez v6, :cond_de

    goto :goto_f4

    .line 499
    :cond_de
    sget-object v6, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-static {v6, v0, v4}, Lcom/jzs/brawl/JZSModMenu;->makeSliderCard(Landroid/content/Context;F[Ljava/lang/String;)Landroid/view/View;

    move-result-object v4

    .line 500
    new-instance v6, Landroid/widget/LinearLayout$LayoutParams;

    invoke-direct {v6, v10, v12}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 501
    invoke-static {v0, v9}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v7

    iput v7, v6, Landroid/widget/LinearLayout$LayoutParams;->bottomMargin:I

    .line 502
    sget-object v7, Lcom/jzs/brawl/JZSModMenu;->sBody:Landroid/widget/LinearLayout;

    invoke-virtual {v7, v4, v6}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 497
    :goto_f4
    add-int/lit8 v3, v3, 0x1

    goto :goto_ce

    .line 505
    :cond_f7
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->refreshToggleStates()V

    .line 506
    return-void

    .line 457
    :cond_fb
    :goto_fb
    return-void
.end method

.method private static refreshSidebar()V
    .registers 8

    .line 706
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    if-eqz v0, :cond_3d

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-nez v0, :cond_9

    goto :goto_3d

    .line 707
    :cond_9
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    invoke-virtual {v0}, Landroid/widget/LinearLayout;->removeAllViews()V

    .line 708
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v0

    iget v0, v0, Landroid/util/DisplayMetrics;->density:F

    .line 709
    const/4 v1, 0x0

    move v7, v1

    :goto_1c
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    array-length v1, v1

    if-ge v7, v1, :cond_3c

    .line 710
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->sSidebar:Landroid/widget/LinearLayout;

    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    aget-object v4, v3, v7

    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->GROUP_COLORS:[I

    aget v5, v3, v7

    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->GROUPS:[Ljava/lang/String;

    aget-object v3, v3, v7

    invoke-static {v3}, Lcom/jzs/brawl/JZSModMenu;->getGroupCount(Ljava/lang/String;)I

    move-result v6

    move v3, v0

    invoke-static/range {v1 .. v6}, Lcom/jzs/brawl/JZSModMenu;->addSidebarTab(Landroid/app/Activity;Landroid/widget/LinearLayout;FLjava/lang/String;II)V

    .line 709
    add-int/lit8 v7, v7, 0x1

    goto :goto_1c

    .line 712
    :cond_3c
    return-void

    .line 706
    :cond_3d
    :goto_3d
    return-void
.end method

.method private static refreshSubTabs()V
    .registers 11

    .line 389
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    if-eqz v0, :cond_116

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-nez v0, :cond_a

    goto/16 :goto_116

    .line 390
    :cond_a
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    invoke-virtual {v0}, Landroid/widget/LinearLayout;->removeAllViews()V

    .line 391
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v0

    iget v0, v0, Landroid/util/DisplayMetrics;->density:F

    .line 394
    new-instance v1, Ljava/util/ArrayList;

    invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V

    .line 395
    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->FEATURES:[[Ljava/lang/String;

    array-length v3, v2

    const/4 v4, 0x0

    move v5, v4

    :goto_25
    const/4 v6, 0x3

    const/4 v7, 0x2

    if-ge v5, v3, :cond_45

    aget-object v8, v2, v5

    .line 396
    aget-object v7, v8, v7

    sget-object v9, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    invoke-virtual {v7, v9}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v7

    if-eqz v7, :cond_42

    aget-object v7, v8, v6

    invoke-virtual {v1, v7}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    move-result v7

    if-nez v7, :cond_42

    aget-object v6, v8, v6

    invoke-virtual {v1, v6}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 395
    :cond_42
    add-int/lit8 v5, v5, 0x1

    goto :goto_25

    .line 398
    :cond_45
    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->SLIDERS:[[Ljava/lang/String;

    array-length v3, v2

    move v5, v4

    :goto_49
    if-ge v5, v3, :cond_6b

    aget-object v8, v2, v5

    .line 399
    aget-object v9, v8, v7

    sget-object v10, Lcom/jzs/brawl/JZSModMenu;->sCurrentGroup:Ljava/lang/String;

    invoke-virtual {v9, v10}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v9

    if-eqz v9, :cond_68

    .line 400
    array-length v9, v8

    if-le v9, v6, :cond_5d

    aget-object v8, v8, v7

    goto :goto_5f

    :cond_5d
    const-string v8, "General"

    .line 401
    :goto_5f
    invoke-virtual {v1, v8}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    move-result v9

    if-nez v9, :cond_68

    invoke-virtual {v1, v8}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 398
    :cond_68
    add-int/lit8 v5, v5, 0x1

    goto :goto_49

    .line 406
    :cond_6b
    const-string v2, "All"

    invoke-virtual {v1, v4, v2}, Ljava/util/ArrayList;->add(ILjava/lang/Object;)V

    .line 408
    sget-object v3, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    invoke-virtual {v3}, Ljava/lang/String;->isEmpty()Z

    move-result v3

    if-eqz v3, :cond_7a

    sput-object v2, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    .line 410
    :cond_7a
    invoke-virtual {v1}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :goto_7e
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_115

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/String;

    .line 411
    new-instance v3, Landroid/widget/TextView;

    sget-object v5, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-direct {v3, v5}, Landroid/widget/TextView;-><init>(Landroid/content/Context;)V

    .line 412
    invoke-virtual {v3, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 413
    const v5, 0x4059999a    # 3.4f

    invoke-static {v0, v5}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v5

    invoke-virtual {v3, v7, v5}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 414
    const/16 v5, 0x11

    invoke-virtual {v3, v5}, Landroid/widget/TextView;->setGravity(I)V

    .line 415
    const/16 v5, 0xc

    invoke-static {v0, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    const/4 v8, 0x6

    invoke-static {v0, v8}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v9

    invoke-static {v0, v5}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v5

    invoke-static {v0, v8}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v10

    invoke-virtual {v3, v6, v9, v5, v10}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 417
    sget-object v5, Lcom/jzs/brawl/JZSModMenu;->sCurrentSubGroup:Ljava/lang/String;

    invoke-virtual {v2, v5}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v5

    .line 418
    if-eqz v5, :cond_c3

    const/4 v6, -0x1

    goto :goto_c6

    :cond_c3
    const v6, -0x84786c

    :goto_c6
    invoke-virtual {v3, v6}, Landroid/widget/TextView;->setTextColor(I)V

    .line 419
    new-instance v6, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v6}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 420
    if-eqz v5, :cond_d4

    const v9, 0x40e94560

    goto :goto_d5

    :cond_d4
    move v9, v4

    :goto_d5
    invoke-virtual {v6, v9}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 421
    const/high16 v9, 0x40c00000    # 6.0f

    invoke-static {v0, v9}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v9

    invoke-virtual {v6, v9}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 422
    if-eqz v5, :cond_ee

    const/4 v9, 0x1

    invoke-static {v0, v9}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v9

    const v10, -0x16baa0

    invoke-virtual {v6, v9, v10}, Landroid/graphics/drawable/GradientDrawable;->setStroke(II)V

    .line 423
    :cond_ee
    invoke-virtual {v3, v6}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 424
    const-string v6, "sans-serif-medium"

    invoke-static {v6, v5}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v5

    invoke-virtual {v3, v5}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 426
    new-instance v5, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v6, -0x2

    invoke-direct {v5, v6, v6}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 427
    invoke-static {v0, v8}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v6

    iput v6, v5, Landroid/widget/LinearLayout$LayoutParams;->rightMargin:I

    .line 428
    sget-object v6, Lcom/jzs/brawl/JZSModMenu;->sSubTabs:Landroid/widget/LinearLayout;

    invoke-virtual {v6, v3, v5}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 430
    new-instance v5, Lcom/jzs/brawl/JZSModMenu$5;

    invoke-direct {v5, v2}, Lcom/jzs/brawl/JZSModMenu$5;-><init>(Ljava/lang/String;)V

    invoke-virtual {v3, v5}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 438
    goto/16 :goto_7e

    .line 439
    :cond_115
    return-void

    .line 389
    :cond_116
    :goto_116
    return-void
.end method

.method private static refreshToggleStates()V
    .registers 5

    .line 715
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-nez v0, :cond_5

    return-void

    .line 716
    :cond_5
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v0

    iget v0, v0, Landroid/util/DisplayMetrics;->density:F

    .line 717
    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sToggleViews:Ljava/util/ArrayList;

    invoke-virtual {v1}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :goto_17
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_35

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, [Ljava/lang/Object;

    .line 718
    const/4 v3, 0x0

    aget-object v3, v2, v3

    check-cast v3, Landroid/widget/TextView;

    .line 719
    const/4 v4, 0x1

    aget-object v2, v2, v4

    check-cast v2, Ljava/lang/String;

    .line 720
    invoke-static {v2}, Lcom/jzs/brawl/JZSModMenu;->isFeatureOn(Ljava/lang/String;)Z

    move-result v2

    .line 721
    invoke-static {v3, v2, v0}, Lcom/jzs/brawl/JZSModMenu;->updateBadge(Landroid/widget/TextView;ZF)V

    .line 722
    goto :goto_17

    .line 723
    :cond_35
    return-void
.end method

.method private static saveSettings()V
    .registers 5

    .line 818
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    if-eqz v0, :cond_58

    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    if-nez v0, :cond_9

    goto :goto_58

    .line 820
    :cond_9
    :try_start_9
    new-instance v0, Ljava/io/File;

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sActivity:Landroid/app/Activity;

    invoke-virtual {v1}, Landroid/app/Activity;->getFilesDir()Ljava/io/File;

    move-result-object v1

    const-string v2, "jzs_mod_settings.txt"

    invoke-direct {v0, v1, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 821
    invoke-virtual {v0}, Ljava/io/File;->getParentFile()Ljava/io/File;

    move-result-object v1

    invoke-virtual {v1}, Ljava/io/File;->mkdirs()Z

    .line 822
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    .line 823
    sget-object v2, Lcom/jzs/brawl/JZSModMenu;->sSettingLines:Ljava/util/ArrayList;

    invoke-virtual {v2}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v2

    :goto_28
    invoke-interface {v2}, Ljava/util/Iterator;->hasNext()Z

    move-result v3

    if-eqz v3, :cond_3e

    invoke-interface {v2}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 824
    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    const/16 v4, 0xa

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    .line 825
    goto :goto_28

    .line 826
    :cond_3e
    new-instance v2, Ljava/io/FileOutputStream;

    const/4 v3, 0x0

    invoke-direct {v2, v0, v3}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;Z)V

    .line 827
    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "UTF-8"

    invoke-virtual {v0, v1}, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B

    move-result-object v0

    invoke-virtual {v2, v0}, Ljava/io/FileOutputStream;->write([B)V

    .line 828
    invoke-virtual {v2}, Ljava/io/FileOutputStream;->close()V
    :try_end_54
    .catch Ljava/lang/Exception; {:try_start_9 .. :try_end_54} :catch_55

    goto :goto_56

    .line 829
    :catch_55
    move-exception v0

    :goto_56
    nop

    .line 830
    return-void

    .line 818
    :cond_58
    :goto_58
    return-void
.end method

.method private static setFeature(Ljava/lang/String;Z)V
    .registers 7

    .line 842
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->loadSettings()Ljava/util/ArrayList;

    move-result-object v0

    .line 843
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    if-eqz p1, :cond_18

    const-string p1, "1"

    goto :goto_1a

    :cond_18
    const-string p1, "0"

    :goto_1a
    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 844
    const/4 v1, 0x0

    :goto_23
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v3

    if-ge v1, v3, :cond_50

    .line 845
    invoke-virtual {v0, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v4, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v3

    if-eqz v3, :cond_4d

    .line 846
    invoke-virtual {v0, v1, p1}, Ljava/util/ArrayList;->set(ILjava/lang/Object;)Ljava/lang/Object;

    .line 847
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->saveSettings()V

    .line 848
    return-void

    .line 844
    :cond_4d
    add-int/lit8 v1, v1, 0x1

    goto :goto_23

    .line 851
    :cond_50
    invoke-virtual {v0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 852
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->saveSettings()V

    .line 853
    return-void
.end method

.method private static setIntPref(Ljava/lang/String;I)V
    .registers 7

    .line 867
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->loadSettings()Ljava/util/ArrayList;

    move-result-object v0

    .line 868
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 869
    const/4 v1, 0x0

    :goto_1c
    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v3

    if-ge v1, v3, :cond_49

    .line 870
    invoke-virtual {v0, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v4, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v3

    if-eqz v3, :cond_46

    .line 871
    invoke-virtual {v0, v1, p1}, Ljava/util/ArrayList;->set(ILjava/lang/Object;)Ljava/lang/Object;

    .line 872
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->saveSettings()V

    .line 873
    return-void

    .line 869
    :cond_46
    add-int/lit8 v1, v1, 0x1

    goto :goto_1c

    .line 876
    :cond_49
    invoke-virtual {v0, p1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 877
    invoke-static {}, Lcom/jzs/brawl/JZSModMenu;->saveSettings()V

    .line 878
    return-void
.end method

.method public static show(Landroid/content/Context;)V
    .registers 2

    .line 907
    instance-of v0, p0, Landroid/app/Activity;

    if-eqz v0, :cond_9

    .line 908
    check-cast p0, Landroid/app/Activity;

    invoke-static {p0}, Lcom/jzs/brawl/JZSModMenu;->attach(Landroid/app/Activity;)V

    .line 910
    :cond_9
    return-void
.end method

.method private static startRainbow()V
    .registers 2

    .line 779
    new-instance v0, Lcom/jzs/brawl/JZSModMenu$11;

    invoke-direct {v0}, Lcom/jzs/brawl/JZSModMenu$11;-><init>()V

    sput-object v0, Lcom/jzs/brawl/JZSModMenu;->sRainbow:Ljava/lang/Runnable;

    .line 789
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sHandler:Landroid/os/Handler;

    sget-object v1, Lcom/jzs/brawl/JZSModMenu;->sRainbow:Ljava/lang/Runnable;

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 790
    return-void
.end method

.method private static togglePanel()V
    .registers 4

    .line 682
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    if-nez v0, :cond_5

    return-void

    .line 683
    :cond_5
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0}, Landroid/widget/FrameLayout;->getVisibility()I

    move-result v0

    const/4 v1, 0x0

    if-nez v0, :cond_10

    const/4 v0, 0x1

    goto :goto_11

    :cond_10
    move v0, v1

    .line 684
    :goto_11
    const/4 v2, 0x0

    const v3, 0x3f666666    # 0.9f

    if-eqz v0, :cond_3c

    .line 685
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0}, Landroid/widget/FrameLayout;->animate()Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0, v2}, Landroid/view/ViewPropertyAnimator;->alpha(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0, v3}, Landroid/view/ViewPropertyAnimator;->scaleX(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0, v3}, Landroid/view/ViewPropertyAnimator;->scaleY(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    const-wide/16 v1, 0xc8

    invoke-virtual {v0, v1, v2}, Landroid/view/ViewPropertyAnimator;->setDuration(J)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    new-instance v1, Lcom/jzs/brawl/JZSModMenu$10;

    invoke-direct {v1}, Lcom/jzs/brawl/JZSModMenu$10;-><init>()V

    .line 686
    invoke-virtual {v0, v1}, Landroid/view/ViewPropertyAnimator;->withEndAction(Ljava/lang/Runnable;)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    .line 692
    invoke-virtual {v0}, Landroid/view/ViewPropertyAnimator;->start()V

    goto :goto_79

    .line 694
    :cond_3c
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0, v1}, Landroid/widget/FrameLayout;->setVisibility(I)V

    .line 695
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sDimBg:Landroid/view/View;

    invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V

    .line 696
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sFab:Landroid/widget/FrameLayout;

    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/FrameLayout;->setVisibility(I)V

    .line 697
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0, v2}, Landroid/widget/FrameLayout;->setAlpha(F)V

    .line 698
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0, v3}, Landroid/widget/FrameLayout;->setScaleX(F)V

    .line 699
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0, v3}, Landroid/widget/FrameLayout;->setScaleY(F)V

    .line 700
    sget-object v0, Lcom/jzs/brawl/JZSModMenu;->sPanel:Landroid/widget/FrameLayout;

    invoke-virtual {v0}, Landroid/widget/FrameLayout;->animate()Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    const/high16 v1, 0x3f800000    # 1.0f

    invoke-virtual {v0, v1}, Landroid/view/ViewPropertyAnimator;->alpha(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/ViewPropertyAnimator;->scaleX(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/ViewPropertyAnimator;->scaleY(F)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    const-wide/16 v1, 0xfa

    invoke-virtual {v0, v1, v2}, Landroid/view/ViewPropertyAnimator;->setDuration(J)Landroid/view/ViewPropertyAnimator;

    move-result-object v0

    invoke-virtual {v0}, Landroid/view/ViewPropertyAnimator;->start()V

    .line 702
    :goto_79
    return-void
.end method

.method private static updateBadge(Landroid/widget/TextView;ZF)V
    .registers 7

    .line 562
    if-eqz p1, :cond_5

    const-string v0, "ON"

    goto :goto_7

    :cond_5
    const-string v0, "OFF"

    :goto_7
    invoke-virtual {p0, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 563
    const v0, 0x40333333    # 2.8f

    invoke-static {p2, v0}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result v0

    const/4 v1, 0x2

    invoke-virtual {p0, v1, v0}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 564
    const/4 v0, -0x1

    invoke-virtual {p0, v0}, Landroid/widget/TextView;->setTextColor(I)V

    .line 565
    const-string v0, "sans-serif-black"

    const/4 v2, 0x1

    invoke-static {v0, v2}, Landroid/graphics/Typeface;->create(Ljava/lang/String;I)Landroid/graphics/Typeface;

    move-result-object v0

    invoke-virtual {p0, v0}, Landroid/widget/TextView;->setTypeface(Landroid/graphics/Typeface;)V

    .line 566
    const/16 v0, 0x11

    invoke-virtual {p0, v0}, Landroid/widget/TextView;->setGravity(I)V

    .line 567
    const/4 v0, 0x6

    invoke-static {p2, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v2

    invoke-static {p2, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v3

    invoke-static {p2, v0}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v0

    invoke-static {p2, v1}, Lcom/jzs/brawl/JZSModMenu;->dp(FI)I

    move-result v1

    invoke-virtual {p0, v2, v3, v0, v1}, Landroid/widget/TextView;->setPadding(IIII)V

    .line 569
    new-instance v0, Landroid/graphics/drawable/GradientDrawable;

    invoke-direct {v0}, Landroid/graphics/drawable/GradientDrawable;-><init>()V

    .line 570
    if-eqz p1, :cond_47

    const p1, -0xff339a

    goto :goto_4a

    :cond_47
    const p1, -0x84786c

    :goto_4a
    invoke-virtual {v0, p1}, Landroid/graphics/drawable/GradientDrawable;->setColor(I)V

    .line 571
    const/high16 p1, 0x40800000    # 4.0f

    invoke-static {p2, p1}, Lcom/jzs/brawl/JZSModMenu;->dpf(FF)F

    move-result p1

    invoke-virtual {v0, p1}, Landroid/graphics/drawable/GradientDrawable;->setCornerRadius(F)V

    .line 572
    invoke-virtual {p0, v0}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    .line 573
    return-void
.end method

.method private static vibrate(Landroid/content/Context;I)V
    .registers 4

    .line 900
    :try_start_0
    const-string v0, "vibrator"

    invoke-virtual {p0, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Landroid/os/Vibrator;

    .line 901
    if-eqz p0, :cond_16

    int-to-long v0, p1

    const/16 p1, 0x28

    invoke-static {v0, v1, p1}, Landroid/os/VibrationEffect;->createOneShot(JI)Landroid/os/VibrationEffect;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroid/os/Vibrator;->vibrate(Landroid/os/VibrationEffect;)V
    :try_end_14
    .catchall {:try_start_0 .. :try_end_14} :catchall_15

    goto :goto_16

    .line 902
    :catchall_15
    move-exception p0

    :cond_16
    :goto_16
    nop

    .line 903
    return-void
.end method
